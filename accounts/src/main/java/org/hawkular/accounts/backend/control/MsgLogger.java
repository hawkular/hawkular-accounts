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
package org.hawkular.accounts.backend.control;

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
@ValidIdRange(min = 110000, max = 119999)
public interface MsgLogger {
    MsgLogger LOGGER = Logger.getMessageLogger(MsgLogger.class, MsgLogger.class.getPackage().getName());

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 110000, value = "Started setting up Hawkular Accounts")
    void infoStartedSetupAccounts();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 110001, value = "Finished setting up Hawkular Accounts")
    void infoFinishedSetupAccounts();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 110002, value = "Starting database update")
    void infoStartedDatabaseUpdated();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 110003, value = "Finished database update")
    void infoFinishedDatabaseUpdate();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 110004, value = "Invitation [%s] submitted. Token: [%s]")
    void invitationSubmitted(String invitationId, String token);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 110005, value = "Invitation [%s] is being reused by a different user [%s]. It was accepted by: [%s]")
    void invitationReused(String invitationId, String userTryingToUse, String acceptedBy);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 110006, value = "An exception has occurred while sending the message for invitation [%s]." +
            " Exception: [%s]")
    void invitationExceptionSendingMessage(String invitationId, String message);
}
