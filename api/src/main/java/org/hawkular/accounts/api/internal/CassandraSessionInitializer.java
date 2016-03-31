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
package org.hawkular.accounts.api.internal;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.hawkular.accounts.api.internal.impl.MsgLogger;
import org.hawkular.accounts.common.internal.CassandraSessionCallable;

import com.datastax.driver.core.Session;

/**
 * Responsible for producing a ready-to-be-consumed Cassandra session. Initializes the schema if needed.
 *
 * @author Juraci Paixão Kröhling
 */
@Startup
@Singleton
@ApplicationScoped
@PermitAll
public class CassandraSessionInitializer {
    MsgLogger logger = MsgLogger.LOGGER;

    private Future<Session> sessionFuture;

    @Resource
    private ManagedExecutorService executor;

    @Inject
    CassandraSessionCallable cassandraSessionCallable;

    /**
     * Sends the order to build the session as soon as this is constructed. The actual execution happens in
     * background, as we don't want to delay the boot of the application.
     */
    @PostConstruct
    public void init() {
        sessionFuture = executor.submit(cassandraSessionCallable);
    }

    @PreDestroy
    public void destroy() {
        logger.shuttingDownCassandraDriver();
        try {
            sessionFuture.get().getCluster().closeAsync();
        } catch (InterruptedException | ExecutionException e) {
            logger.failedToShutdownDriver(e);
        }
    }

    /**
     * Produces the Cassandra session, waiting for the background job to finish if needed.
     * @return the Cassandra session, ready to be consumed.
     */
    @Produces @ApplicationScoped
    public Session getSession() {
        try {
            // on the first boot, this might take some time to return, specially for "during the boot calls"
            // but for subsequent calls, this should be quite fast.
            Session session = sessionFuture.get();
            logger.cassandraSessionAcquired();

            // now that we have a session, let's first initialize the schema...
            // as of now, our CQL script is idempotent, so, we just execute it.
            InputStream input = getClass().getResourceAsStream("/hawkular_accounts.cql");
            //noinspection Duplicates
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
                String content = buffer.lines().collect(Collectors.joining("\n"));

                // we split the statements by "--#", as it's done in other Hawkular projects.
                for (String cql : content.split("(?m)^-- #.*$")) {
                    if (!cql.startsWith("--")) { // if it doesn't look like a comment, execute it
                        logger.debugf("Executing CQL [%s]", cql);
                        session.execute(cql);
                    }
                }
            } catch (Exception e) {
                logger.failedToInitializeSchema(e);
            }
            return session;
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("Could not get the initialized session.");
        }
    }
}
