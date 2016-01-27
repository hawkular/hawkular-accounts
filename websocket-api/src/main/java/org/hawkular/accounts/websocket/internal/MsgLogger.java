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
package org.hawkular.accounts.websocket.internal;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.ValidIdRange;

/**
 * JBoss Logging integration, with the possible messages that we have for this backend.
 *
 * @author Juraci Paixão Kröhling
 */
@org.jboss.logging.annotations.MessageLogger(projectCode = "HAWKACC")
@ValidIdRange(min = 120000, max = 129999)
public interface MsgLogger {
    MsgLogger LOGGER = Logger.getMessageLogger(MsgLogger.class, MsgLogger.class.getPackage().getName());

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 120001, value = "Trying to authenticate based on the contents of the message.")
    void messageBasedAuth();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 120002, value = "Trying to authenticate based on the token [%s], for persona [%s].")
    void tokenBasedAuth(String tokenId, String personaId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 120003, value = "Trying to authenticate based on the credentials. Username: [%s].")
    void credentialsBasedAuth(String username);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 120004, value = "Session [%s] is in the cache and still valid. Authentication finished.")
    void sessionInCache(String sessionId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 120005, value = "Retrieving token for username [%s].")
    void retrievingTokenForCredentials(String username);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 120006, value = "Cached session is for different persona. Cached: [%s], persona from token: [%s].")
    void personaMismatch(String cachedPersona, String tokenPersona);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 120007, value = "Cached session is backed by a different token. Cached token: [%s], token " +
            "from message: [%s].")
    void backingTokenChanged(String cachedToken, String tokenFromMessage);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 120008, value = "Is the token still within the expiration timestamp (still valid)? [%b]")
    void isTokenStillValid(boolean stillValid);
}
