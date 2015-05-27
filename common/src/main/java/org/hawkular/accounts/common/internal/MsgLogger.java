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
}
