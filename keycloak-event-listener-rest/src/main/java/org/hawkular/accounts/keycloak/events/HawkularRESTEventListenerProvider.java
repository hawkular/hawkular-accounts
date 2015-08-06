/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.accounts.keycloak.events;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Set;
import java.util.UUID;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;

/**
 * @author Juraci Paixão Kröhling
 */
public class HawkularRESTEventListenerProvider implements EventListenerProvider {
    private final Set<EventType> excludedEvents;

    public HawkularRESTEventListenerProvider(Set<EventType> excludedEvents) {
        this.excludedEvents = excludedEvents;
    }

    @Override
    public void onEvent(Event event) {
        if (excludedEvents != null && excludedEvents.contains(event.getType())) {
            return;
        }

        try {
            publishToHawkular(event);
        } catch (Exception e) {
            String message = "WARNING: Couldn't publish event to Hawkular. Event: " + event.toString();
            message += ". Cause: " + e.getMessage();

            // yes, System.out.println :-) This gets logged back via jboss-logging into the main server log,
            // and as this is deployed as a module, we don't get in trouble with classpath/module dependencies.
            System.out.println(message);
        }
    }

    public void publishToHawkular(Event event) throws Exception {
        if (event.getUserId() == null) {
            return;
        }

        String endpointUrl = System.getProperty("hawkular.events.listener.rest.endpoint");
        String eventId = UUID.randomUUID().toString();
        String userId = event.getUserId();
        String action = event.getType().name();

        StringBuilder sb = new StringBuilder();
        sb.append("eventId=").append(URLEncoder.encode(eventId, "UTF-8"));
        sb.append("&userId=").append(URLEncoder.encode(userId, "UTF-8"));
        sb.append("&action=").append(URLEncoder.encode(action, "UTF-8"));

        HttpURLConnection connection = (HttpURLConnection) new URL(endpointUrl).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        try (PrintWriter out = new PrintWriter(connection.getOutputStream())) {
            out.print(sb.toString());
        }

        StringBuilder response = new StringBuilder();

        int statusCode;
        try {
            statusCode = connection.getResponseCode();
        } catch (SocketTimeoutException timeoutException) {
            String message = "WARNING: Timed out while trying to reach the Hawkular server. Event: " + event.toString();

            // yes, System.out.println :-) This gets logged back via jboss-logging into the main server log,
            // and as this is deployed as a module, we don't get in trouble with classpath/module dependencies.
            System.out.println(message);
            return;
        }

        InputStream inputStream;
        if (statusCode < 300) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            for (String line; (line = reader.readLine()) != null;) {
                response.append(line);
            }
            inputStream.close();
        }

        if (statusCode >= 300) {
            String message = "WARNING: Hawkular didn't process the event correctly" +
                    ". Status code: " + statusCode +
                    ". Response: " + response.toString();

            // yes, System.out.println :-) This gets logged back via jboss-logging into the main server log,
            // and as this is deployed as a module, we don't get in trouble with classpath/module dependencies.
            System.out.println(message);
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
    }

    @Override
    public void close() {
    }
}
