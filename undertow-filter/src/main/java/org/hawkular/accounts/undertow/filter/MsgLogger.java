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
package org.hawkular.accounts.undertow.filter;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.ValidIdRange;

/**
 * JBoss Logging integration, with the possible messages that we have for this module.
 *
 * @author Juraci Paixão Kröhling
 */
@org.jboss.logging.annotations.MessageLogger(projectCode = "HAWKSECSTORE")
@ValidIdRange(min = 100000, max = 109999)
public interface MsgLogger {
    MsgLogger LOGGER = Logger.getMessageLogger(MsgLogger.class, MsgLogger.class.getPackage().getName());

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 100000, value = "Secret Store not enabled. Bypassing all requests.")
    void secretStoreNotEnabled();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 100001, value = "Secret Store enabled. Checking all incoming requests.")
    void secretStoreEnabled();

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 100002, value = "The secret-store's Keycloak client name was not set! Check the documentation. All " +
            "requests that would be intercepted by the filter WILL fail with 'Forbidden' status.")
    void clientNotSet();

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 100003, value = "The secret-store's Keycloak secret key was not set! Check the documentation. All " +
            "requests that would be intercepted by the filter WILL fail with 'Forbidden' status.")
    void secretNotSet();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 100004, value = "Returning Forbidden status code, as the secret-store's Keycloak client name is not" +
            " set.")
    void forbiddenClientNotSet();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 100005, value = "Returning Forbidden status code, as the secret-store's Keycloak secret is not set.")
    void forbiddenSecretNotSet();

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 100006, value = "Timeout reached when contacting the Keycloak server.")
    void timeoutContactingKeycloakServer();

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 100007, value = "Secret Store received a response from Keycloak that doesn't include a bearer token.")
    void invalidResponseFromServer();

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 100008, value = "Secret Store a bearer response from Keycloak without the token itself!")
    void invalidBearerTokenFromServer();

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 100009, value = "Secret Store received an error response from Keycloak: %s")
    void errorResponseFromServer(String errorMessage);

}
