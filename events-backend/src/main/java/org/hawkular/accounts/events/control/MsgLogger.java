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
package org.hawkular.accounts.events.control;

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
@ValidIdRange(min = 130000, max = 139999)
public interface MsgLogger {
    MsgLogger LOGGER = Logger.getMessageLogger(MsgLogger.class, MsgLogger.class.getPackage().getName());

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 130000, value = "Event received by Accounts. Action: [%s], userId: [%s], eventId: [%s]")
    void eventReceived(String action, String userId, String eventId);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 130001, value = "Event published to the queue by Accounts")
    void eventPublished();
}
