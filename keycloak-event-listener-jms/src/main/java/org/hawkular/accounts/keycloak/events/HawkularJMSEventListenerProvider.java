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

import java.util.Set;
import java.util.UUID;

import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;

/**
 * @author Juraci Paixão Kröhling
 */
public class HawkularJMSEventListenerProvider implements EventListenerProvider {
    private final Set<EventType> excludedEvents;

    private Topic topic;
    private TopicConnectionFactory connectionFactory;

    public HawkularJMSEventListenerProvider(Set<EventType> excludedEvents) {
        this.excludedEvents = excludedEvents;

        try {
            Context ctx = new InitialContext();
            this.topic = (Topic) ctx.lookup("java:/topic/HawkularAccountsEvents");
            this.connectionFactory = (TopicConnectionFactory) ctx.lookup("java:/HawkularBusConnectionFactory");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
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

        String eventId = UUID.randomUUID().toString();
        String userId = event.getUserId();
        String action = event.getType().name();

        try {
            TopicConnection connection = connectionFactory.createTopicConnection();
            Session session = connection.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(topic);
            Message message = session.createMessage();
            message.setStringProperty("action", action);
            message.setStringProperty("userId", userId);
            message.setStringProperty("eventId", eventId);
            messageProducer.send(message);
            messageProducer.close();
            session.close();
            connection.close();
        } catch (Exception e) {
            String message = "WARNING: Couldn't publish event to Hawkular. Event: " + event.toString();
            message += ". Cause: " + e.getMessage();

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
