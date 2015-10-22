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
package org.hawkular.accounts.api.internal;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.hawkular.accounts.api.internal.impl.MsgLogger;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;

/**
 * Provides CDI producers for resources required by the API.
 *
 * @author Juraci Paixão Kröhling
 */
@ApplicationScoped
public class ApplicationResources {
    private Map<BoundStatements, PreparedStatement> statements = new HashMap<>(BoundStatements.values().length);
    private MsgLogger logger = MsgLogger.LOGGER;

    @Inject
    Session session;

    /**
     * Prepares the statements from BoundStatements and put them into a cache, for later consumption.
     * If a query fails to prepare for some reason, it's skipped from the cache. This effectively means that any code
     * using the failed statement will also fail, but everything else should work.
     */
    @PostConstruct
    public void buildStatements() {
        for (BoundStatements statement : BoundStatements.values()) {
            if (statement.equals(BoundStatements.DEFAULT)) {
                continue; // this one, we don't want to add to the cache
            }

            try {
                statements.put(statement, session.prepare(statement.getValue()));
            } catch (InvalidQueryException e) {
                logger.couldNotPrepareQuery(statement.getValue(), e);
            }
        }
    }

    /**
     * CDI producer for @NamedStatement annotations. Meant to be consumed only by the CDI implementation.
     * @param injectionPoint    the injection point of where the annotation is
     * @return  the BoundStatement that matches the annotation's value.
     */
    @Produces @NamedStatement
    public BoundStatement produceStatementByName(InjectionPoint injectionPoint) {
        NamedStatement annotation = injectionPoint.getAnnotated().getAnnotation(NamedStatement.class);
        BoundStatements stmtName = annotation.value();
        return getBoundStatement(stmtName);
    }

    /**
     * Retrieves a new BoundStatement based on the BoundStatements's enum item name.
     *
     * @param statement    the statement's entry on the enum
     * @return the BoundStatement for the entry
     */
    public BoundStatement getBoundStatement(BoundStatements statement) {
        if (statements.size() == 0) {
            buildStatements();
        }
        // TODO: think more about the implications of this... for instance:
        // - What if a non-stateless bean injects this? We'll end up having two threads (at different times,
        // possibly) with the same bound statement, so, possibly with dirty objects.
        // - Our stateless beans should get a new instance for each request, but make sure that's the case
        // - How could we make sure, absolutely sure, that injection points *always* get a clean statement?
        return new BoundStatement(statements.get(statement));
    }

    /**
     * Non-CDI consumers can use this to set the session before retrieving BoundStatements.
     * @param session    the session to use when preparing the statements
     */
    public void setSession(Session session) {
        this.session = session;
    }
}
