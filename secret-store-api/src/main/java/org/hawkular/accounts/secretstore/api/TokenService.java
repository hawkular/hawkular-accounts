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

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.hawkular.accounts.common.ZonedDateTimeAdapter;
import org.hawkular.accounts.secretstore.api.internal.BoundStatements;
import org.hawkular.accounts.secretstore.api.internal.NamedStatement;
import org.hawkular.accounts.secretstore.api.internal.SecretStore;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * @author Juraci Paixão Kröhling
 */
@Stateless
@PermitAll
public class TokenService {
    @Inject @SecretStore
    Session session;

    @Inject
    ZonedDateTimeAdapter zonedDateTimeAdapter;

    @Inject @NamedStatement(BoundStatements.CREATE)
    Instance<BoundStatement> stmtCreate;

    @Inject @NamedStatement(BoundStatements.GET_BY_ID)
    Instance<BoundStatement> stmtGetById;

    @Inject @NamedStatement(BoundStatements.GET_BY_PRINCIPAL)
    Instance<BoundStatement> stmtGetByPrincipal;

    @Inject @NamedStatement(BoundStatements.REVOKE_BY_ID)
    Instance<BoundStatement> stmtRevokeById;

    public Token create(Token token) {
        UUID id = token.getId();
        String refreshToken = token.getRefreshToken();
        String secret = token.getSecret();
        String principal = token.getPrincipal();
        Date createdAt = zonedDateTimeAdapter.convertToDatabaseColumn(token.getCreatedAt());
        Date updatedAt = zonedDateTimeAdapter.convertToDatabaseColumn(token.getUpdatedAt());
        Map<String, String> attributes = token.getAttributes();
        session.execute(stmtCreate.get().bind(id, refreshToken, secret, principal, attributes, createdAt, updatedAt));
        return token;
    }

    public void revoke(UUID id) {
        session.execute(stmtRevokeById.get().setUUID("id", id));
    }

    public Token getByIdForTrustedConsumers(UUID id) {
        ResultSet resultSet = session.execute(stmtGetById.get().bind(id));
        List<Row> rows = resultSet.all();

        if (rows.size() > 1) {
            throw new IllegalStateException("There are more than one token for this ID!");
        }

        if (rows.size() == 0) {
            return null;
        }

        return getFullTokenFromRow(rows.stream().findFirst().get());
    }

    public Token getByIdForDistribution(UUID id) {
        ResultSet resultSet = session.execute(stmtGetById.get().bind(id));
        List<Row> rows = resultSet.all();

        if (rows.size() > 1) {
            throw new IllegalStateException("There are more than one token for this ID!");
        }

        if (rows.size() == 0) {
            return null;
        }

        return getSecureTokenFromRow(rows.stream().findFirst().get());
    }

    public List<Token> getByPrincipalForTrustedConsumers(String principal) {
        return session
                .execute(stmtGetByPrincipal.get().bind(principal))
                .all()
                .stream()
                .map(this::getFullTokenFromRow)
                .collect(Collectors.toList());
    }

    public List<Token> getByPrincipalForDistribution(String principal) {
        return session
                .execute(stmtGetByPrincipal.get().bind(principal))
                .all()
                .stream()
                .map(this::getSecureTokenFromRow)
                .collect(Collectors.toList());
    }

    public Token validate(UUID key, String secret) {
        // TODO: caching!!
        Token token = getByIdForTrustedConsumers(key);
        if (token == null) {
            // not valid, not even the ID exists...
            return null;
        }
        return token.getSecret().equals(secret) ? token : null;
    }

    private Token getFullTokenFromRow(Row row) {
        Token token = getSecureTokenFromRow(row);
        token.setRefreshToken(row.getString("refreshToken"));
        return token;
    }

    private Token getSecureTokenFromRow(Row row) {
        UUID id = row.getUUID("id");
        ZonedDateTime createdAt = zonedDateTimeAdapter.convertToEntityAttribute(row.getTimestamp("createdAt"));
        ZonedDateTime updatedAt = zonedDateTimeAdapter.convertToEntityAttribute(row.getTimestamp("updatedAt"));
        String secret = row.getString("secret");
        String principal = row.getString("principal");
        Map<String, String> attributes = row.getMap("attributes", String.class, String.class);
        return new Token(id, createdAt, updatedAt, null, secret, attributes, principal);
    }
}
