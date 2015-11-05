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
package org.hawkular.accounts.secretstore.api.internal;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.hawkular.accounts.common.internal.CassandraSessionCallable;

import com.datastax.driver.core.Session;

/**
 * @author Juraci Paixão Kröhling
 */
@Startup
@Singleton
@ApplicationScoped
@PermitAll
public class SecretStoreSchemaInitializer {
    private Future<Session> sessionFuture;

    @Inject
    CassandraSessionCallable cassandraSessionCallable;

    MsgLogger logger = MsgLogger.LOGGER;

    @Resource
    private ManagedExecutorService executor;

    @PostConstruct
    public void init() {
        sessionFuture = executor.submit(cassandraSessionCallable);
    }

    @Produces @ApplicationScoped
    public Session getSession() {
        try {
            Session session = sessionFuture.get();
            InputStream input = getClass().getResourceAsStream("/secret-store.cql");
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
                String content = buffer.lines().collect(Collectors.joining("\n"));
                for (String cql : content.split("(?m)^-- #.*$")) {
                    if (!cql.startsWith("--")) {
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