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
package org.hawkular.accounts.events.listener.internal;

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
@ValidIdRange(min = 140000, max = 149999)
public interface MsgLogger {
    MsgLogger LOGGER = Logger.getMessageLogger(MsgLogger.class, MsgLogger.class.getPackage().getName());

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 140000, value = "Event received by Accounts Message Listener.")
    void eventReceived();

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 140001, value = "An error occurred while trying to process event.")
    void errorProcessingEvent(@Cause Throwable e);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 140002, value = "Event processed by Accounts API: %s, %s, %s")
    void eventProcessed(String action, String eventId, String userId);
}
