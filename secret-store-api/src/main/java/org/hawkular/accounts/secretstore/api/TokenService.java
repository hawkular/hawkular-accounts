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

    public void create(Token token) {
        UUID id = token.getId();
        String refreshToken = token.getRefreshToken();
        String secret = token.getSecret();
        Date createdAt = zonedDateTimeAdapter.convertToDatabaseColumn(token.getCreatedAt());
        Date updatedAt = zonedDateTimeAdapter.convertToDatabaseColumn(token.getUpdatedAt());
        Map<String, String> attributes = token.getAttributes();
        session.execute(stmtCreate.get().bind(id, refreshToken, secret, attributes, createdAt, updatedAt));
    }

    public Token getById(UUID id) {
        ResultSet resultSet = session.execute(stmtGetById.get().bind(id));
        List<Row> rows = resultSet.all();

        if (rows.size() > 1) {
            throw new IllegalStateException("There are more than one token for this ID!");
        }

        if (rows.size() == 0) {
            return null;
        }

        return getTokenFromRow(rows.stream().findFirst().get());
    }

    public Token validate(UUID key, String secret) {
        // TODO: caching!!
        Token token = getById(key);
        if (token == null) {
            // not valid, not even the ID exists...
            return null;
        }
        return token.getSecret().equals(secret) ? token : null;
    }

    private Token getTokenFromRow(Row row) {
        UUID id = row.getUUID("id");
        ZonedDateTime createdAt = zonedDateTimeAdapter.convertToEntityAttribute(row.getTimestamp("createdAt"));
        ZonedDateTime updatedAt = zonedDateTimeAdapter.convertToEntityAttribute(row.getTimestamp("updatedAt"));
        String refreshToken = row.getString("refreshToken");
        String secret = row.getString("secret");
        Map<String, String> attributes = row.getMap("attributes", String.class, String.class);
        return new Token(id, createdAt, updatedAt, refreshToken, secret, attributes);
    }
}
