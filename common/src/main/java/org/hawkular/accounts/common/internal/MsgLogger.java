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
package org.hawkular.accounts.common.internal;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.ValidIdRange;

/**
 * @author Juraci Paixão Kröhling
 */
@MessageLogger(projectCode = "HAWKACC")
@ValidIdRange(min = 150000, max = 159999)
public interface MsgLogger {
    MsgLogger LOGGER = Logger.getMessageLogger(MsgLogger.class, MsgLogger.class.getPackage().getName());

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 150000, value = "List of host synonyms provided. Using the list instead of guessing.")
    void listOfSynonymsProvided();

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 150001, value = "Bound to wildcard address 0.0.0.0, getting a list of all local IPs for Synonyms")
    void allLocalAddressesForSynonyms();

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 150002, value = "Could not process what's the IP for the wildcard host: [%s]")
    void cannotDetermineIPForWildcardHost(String host);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 150003, value = "Could not process what's the IP for the host: [%s]")
    void cannotDetermineIPForHost(String host, @Cause Throwable t);

    @LogMessage(level = Logger.Level.FATAL)
    @Message(id = 150004, value = "Could not process what are our IPs. Host synonyms will *not* work properly")
    void cannotDetermineLocalIPs(@Cause Throwable t);

    @LogMessage(level = Logger.Level.FATAL)
    @Message(id = 150005, value = "Could not connect to Cassandra after enough attempts. Giving up. Reason")
    void cannotConnectToCassandra(@Cause Throwable t);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 150006, value = "Cassandra is not available (yet?). Attempts left: [%d]. Reason")
    void attemptToConnectToCassandraFailed(int attempt, @Cause Throwable t);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 150007, value = "Cassandra nodes to use: [%s]")
    void cassandraNodesToUse(String cassandraNodes);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 150008, value = "Cassandra port to use: [%s]")
    void cassandraPortToUse(String cassandraPort);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 150009, value = "Realm configuration: [%s]")
    void realmConfiguration(String realmConfiguration);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 150010, value = "Parsing realm configuration.")
    void parsingRealmConfiguration();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 150011, value = "auth-server-url-for-backend-requests is set, will use it when talking with " +
            "Keycloak: [%s]")
    void backendUrlIsSet(String url);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 150012, value = "auth-server-url-for-backend-requests is not set. Building it based on information " +
            "we have.")
    void backendUrlIsNotSet();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 150013, value = "auth-server-url is set. Will use it when talking with Keycloak: [%s]")
    void authServerUrlIsSet(String url);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 150014, value = "Setting auth-server-url set to: [%s]")
    void settingAuthServerUrl(String url);

    /**
     * URL parameters and client secret are omitted, due to security concerns
     */
    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 150015, value = "Executing an Auth Server request. URL: [%s] , URL Parameters: omitted, Client ID: " +
            "[%s], Secret: omitted, Method: [%s].")
    void executingAuthServerRequest(String url, String clientId, String method);

    //@Message(id = 150016, value = "Authentication header for username [%s] and password [%s] encodes to [%s].")
    // this message was removed, due to security concerns

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 150017, value = "Request executed. Status code: [%d].")
    void requestExecuted(int statusCode);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 150018, value = "Status code is not a success code. Redirecting error stream of the request to our " +
            "input stream.")
    void statusCodeNotSuccess();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 150019, value = "Status code is a success code. Redirecting input stream of the request to our " +
            "input stream.")
    void statusCodeSuccess();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 150020, value = "Response body: [%s]")
    void responseBody(String response);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 150021, value = "Attempting to get an access token for username [%s].")
    void accessTokenForUsername(String username);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 150022, value = "Attempting to get a refresh token for username [%s].")
    void refreshTokenForUsername(String username);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 150023, value = "Attempting to get an offline token for username [%s].")
    void offlineTokenForUsername(String username);
}
