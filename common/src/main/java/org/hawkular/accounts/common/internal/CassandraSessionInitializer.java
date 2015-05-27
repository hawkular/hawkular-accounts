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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.datastax.driver.core.Session;

/**
 * @author Juraci Paixão Kröhling
 */
@Startup
@Singleton
@ApplicationScoped
@PermitAll
public class CassandraSessionInitializer {
    private Future<Session> sessionFuture;

    @Resource
    private ManagedExecutorService executor;

    @Inject
    CassandraSessionCallable cassandraSessionCallable;

    @PostConstruct
    public void init() {
        sessionFuture = executor.submit(cassandraSessionCallable);
    }

    @Produces @ApplicationScoped
    public Session getSession() {
        try {
            return sessionFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("Could not get the initialized session.");
        }
    }
}
