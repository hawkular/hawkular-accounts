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
package org.hawkular.accounts.secretstore.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.hawkular.accounts.common.ZonedDateTimeAdapter;
import org.hawkular.accounts.secretstore.api.internal.BoundStatements;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Session;

/**
 * @author Juraci Paixão Kröhling
 */
public class TokenTest {

    private Session session;

    @Inject
    private TokenService tokenService;

    @Rule
    public CassandraCQLUnit cassandraCQLUnit = new CassandraCQLUnit(
            new ClassPathCQLDataSet("secret-store.cql", "secretstore")
    );

    @Before
    public void prepare() {
        ZonedDateTimeAdapter adapter = new ZonedDateTimeAdapter();
        session = cassandraCQLUnit.session;

        Instance<BoundStatement> getByIdStatement = mock(Instance.class);
        when(getByIdStatement.get())
                .thenReturn(new BoundStatement(session.prepare(BoundStatements.GET_BY_ID.getValue())));

        Instance<BoundStatement> createStatement = mock(Instance.class);
        when(createStatement.get())
                .thenReturn(new BoundStatement(session.prepare(BoundStatements.CREATE.getValue())));

        tokenService = new TokenService();
        tokenService.session = session;
        tokenService.zonedDateTimeAdapter = adapter;
        tokenService.stmtGetById = getByIdStatement;
        tokenService.stmtCreate = createStatement;
    }

    @Test
    public void testCreateToken() {
        Token token = new Token("my-refresh-token", UUID.randomUUID().toString());
        tokenService.create(token);
    }

    @Test
    public void testRetrieveToken() {
        Token token = new Token("my-refresh-token", UUID.randomUUID().toString());

        UUID id = token.getId();
        tokenService.create(token);

        Token tokenFromDatabase = tokenService.getByIdForTrustedConsumers(id);
        assertNotNull("ID should not be null", token.getId());
        assertNotNull("Refresh token should not be null", token.getRefreshToken());
        assertNotNull("Secret should not be null", token.getSecret());
        assertNotNull("Attributes should not be null", token.getAttributes());
        assertNotNull("Created At should not be null", token.getCreatedAt());
        assertNotNull("Updated At should not be null", token.getUpdatedAt());
        assertEquals("Token from database should be equals to the one we've sent", tokenFromDatabase, token);
    }

    @Test
    public void testPersistAttribute() {
        Token token = new Token("my-refresh-token", UUID.randomUUID().toString());
        UUID id = token.getId();
        String persona = UUID.randomUUID().toString();
        token.addAttribute("persona", persona);
        tokenService.create(token);

        Token tokenFromDatabase = tokenService.getByIdForTrustedConsumers(id);
        assertEquals("Extra attribute from database should have been retrieved",
                persona,
                tokenFromDatabase.getAttribute("persona"));
    }
}
