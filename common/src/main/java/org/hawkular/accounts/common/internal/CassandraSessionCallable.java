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

import java.util.concurrent.Callable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.hawkular.accounts.common.CassandraNodes;
import org.hawkular.accounts.common.CassandraPort;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.Session;

/**
 * @author Juraci Paixão Kröhling
 */
@ApplicationScoped
public class CassandraSessionCallable implements Callable<Session> {
    MsgLogger logger = MsgLogger.LOGGER;

    private static final String CASSANDRA_RETRY_ATTEMPTS = "hawkular-accounts.cassandra-retry-attempts";
    private static final String CASSANDRA_RETRY_INTERVAL = "hawkular-accounts.cassandra-retry-interval";

    private static int attempts = Integer.parseInt(System.getProperty(CASSANDRA_RETRY_ATTEMPTS, "5"));
    private static int interval = Integer.parseInt(System.getProperty(CASSANDRA_RETRY_INTERVAL, "2000"));

    @Inject @CassandraPort
    String cqlPort;

    @Inject @CassandraNodes
    String nodes;

    @Override
    public Session call() throws Exception {
        try {
            return new Cluster.Builder()
                    .addContactPoints(nodes.split(","))
                    .withPort(new Integer(cqlPort))
                    .withProtocolVersion(ProtocolVersion.V3)
                    .build().connect();
        } catch (Exception e) {
            if (attempts != 0) {
                logger.attemptToConnectToCassandraFailed(attempts, e);
                attempts--;
                Thread.sleep(interval);
                return call();
            } else {
                logger.cannotConnectToCassandra(e);
                return null;
            }
        }
    }
}
