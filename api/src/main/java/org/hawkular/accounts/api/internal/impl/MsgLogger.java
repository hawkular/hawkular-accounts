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
package org.hawkular.accounts.api.internal.impl;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.ValidIdRange;

/**
 * JBoss Logging integration, with the possible messages that we have for the API.
 *
 * @author Juraci Paixão Kröhling
 */
@MessageLogger(projectCode = "HAWKACC")
@ValidIdRange(min = 100000, max = 109999)
public interface MsgLogger {
    MsgLogger LOGGER = Logger.getMessageLogger(MsgLogger.class, MsgLogger.class.getPackage().getName());

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 100000, value = "Could not process prepare query: [%s]")
    void couldNotPrepareQuery(String query, @Cause Throwable t);

    @LogMessage(level = Logger.Level.FATAL)
    @Message(id = 100001, value = "Failed to initialize Cassandra's schema for Accounts. Reason")
    void failedToInitializeSchema(@Cause Throwable t);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 100002, value = "Shutting down Cassandra driver for Accounts")
    void shuttingDownCassandraDriver();

    @LogMessage(level = Logger.Level.FATAL)
    @Message(id = 100003, value = "Failed to properly shutdown the Cassandra driver for Accounts. Reason")
    void failedToShutdownDriver(@Cause Throwable t);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 100004, value = "Organization [%s] created. ID: [%s]")
    void organizationCreated(String organizationName, String organizationId);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 100005, value = "Join request from [%s] to join organization [%s] created. ID: [%s]")
    void joinRequestCreated(String userId, String organizationId, String joinRequestId);
}
