/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
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
package org.hawkular.accounts.common;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Base64;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.hawkular.accounts.common.internal.MsgLogger;

/**
 * @author Juraci Paixão Kröhling
 */
@ApplicationScoped
public class AuthServerRequestExecutor {
    MsgLogger logger = MsgLogger.LOGGER;

    @Inject @RealmResourceName
    private String clientId;

    @Inject @RealmResourceSecret
    private String secret;

    public String execute(String url, String method) throws Exception {
        return execute(url, null, clientId, secret, method);
    }

    public String execute(String url, String urlParameters, String method) throws Exception {
        return execute(url, urlParameters, clientId, secret, method);
    }

    public String execute(String url, String clientId, String secret, String method) throws Exception {
        return execute(url, null, clientId, secret, method);
    }

    /**
     * Performs an HTTP call to the Keycloak server, returning the server's response as String.
     * @param url              the full URL to call, including protocol, host, port and path.
     * @param urlParameters    the HTTP Query Parameters properly encoded and without the leading "?".
     * @param clientId         the OAuth client ID.
     * @param secret           the OAuth client secret.
     * @param method           the HTTP method to use (GET or POST). If anything other than POST is sent, GET is used.
     * @return                 a String with the response from the Keycloak server, in both success and error scenarios
     * @throws Exception       if communication problems with the Keycloak server occurs.
     */
    public String execute(String url, String urlParameters, String clientId, String secret, String method) throws
            Exception {
        logger.executingAuthServerRequest(url, clientId, method);

        HttpURLConnection connection;
        String credentials = clientId + ":" + secret;
        String authorizationHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

        if ("POST".equalsIgnoreCase(method)) {
            connection =  (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Authorization", authorizationHeader);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            if (null != urlParameters) {
                try (PrintWriter out = new PrintWriter(connection.getOutputStream())) {
                    out.print(urlParameters);
                }
            }
        } else {
            connection =  (HttpURLConnection) new URL(url + "?" + urlParameters).openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Authorization", authorizationHeader);
        }

        int timeout = Integer.parseInt(System.getProperty("org.hawkular.accounts.http.timeout", "5000"));
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);

        StringBuilder response = new StringBuilder();

        int statusCode;
        try {
            statusCode = connection.getResponseCode();
            logger.requestExecuted(statusCode);
        } catch (SocketTimeoutException timeoutException) {
            throw new UsernamePasswordConversionException("Timed out when trying to contact the Keycloak server.");
        }

        InputStream inputStream;
        if (statusCode < 300) {
            logger.statusCodeSuccess();
            inputStream = connection.getInputStream();
        } else {
            logger.statusCodeNotSuccess();
            inputStream = connection.getErrorStream();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            for (String line; (line = reader.readLine()) != null;) {
                response.append(line);
            }
        }

        String responseAsString = response.toString();
        logger.responseBody(responseAsString);
        return responseAsString;
    }

}
