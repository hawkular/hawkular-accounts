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
package org.hawkular.accounts.api.internal.impl;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.hawkular.accounts.api.model.BaseEntity;
import org.hawkular.accounts.common.ZonedDateTimeAdapter;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * Provides helper methods for services that interact with Cassandra as data store.
 *
 * @author Juraci Paixão Kröhling
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class BaseServiceImpl<T extends BaseEntity> {
    @Inject
    ZonedDateTimeAdapter zonedDateTimeAdapter;

    @Inject
    Session session;

    /**
     * Returns a record based on its ID.
     * @param uuid         the record's UUID
     * @param statement    the statement to use when retrieving the record.
     * @return the record
     */
    public T getById(UUID uuid, BoundStatement statement) {
        if (null == uuid) {
            throw new IllegalArgumentException("The given ID is invalid (null).");
        }
        return getSingleRecord(statement.setUUID("id", uuid));
    }

    /**
     * Runs a prepared statement that is supposed to retrieve only one record, and gives result as an instance of T.
     * @param statement    the statement ready to be executed
     * @return an T instance that represents the record resulting from the statement
     */
    T getSingleRecord(BoundStatement statement) {
        Row row = session.execute(statement).one();
        if (null == row) {
            return null;
        }
        return getFromRow(row);
    }

    /**
     * Runs a prepared statement that is supposed to retrieve one or more records and gives the results as a list of T.
     * @param statement    the statement ready to be executed
     * @return a List of T according to the results of the query
     */
    List<T> getList(BoundStatement statement) {
        return getFromRows(session.execute(statement).all());
    }

    /**
     * Converts a List of Row into a List of T.
     * @param rows    the List of Row to be converted
     * @return a List of T
     */
    List<T> getFromRows(List<Row> rows) {
        return rows
                .stream()
                .map(this::getFromRow)
                .collect(Collectors.toList());
    }

    /**
     * Updates T, binds the basic parameters to the Statement and executes it, returning the updated T.
     * @param record       the record to be updated
     * @param statement    the statement with the other fields already bound.
     * @return the updated record
     */
    T update(T record, BoundStatement statement) {
        record.setUpdatedAt();
        statement.setTimestamp("updatedAt", zonedDateTimeAdapter.convertToDatabaseColumn(record.getUpdatedAt()));
        statement.setUUID("id", record.getIdAsUUID());
        session.execute(statement);
        return record;
    }

    /**
     * Maps the base fields from the Row into the T.Builder.
     * @param row        the row
     * @param builder    T's builder
     */
    void mapBaseFields(Row row, T.Builder builder) {
        UUID id = row.getUUID("id");
        ZonedDateTime createdAt = zonedDateTimeAdapter.convertToEntityAttribute(row.getTimestamp("createdAt"));
        ZonedDateTime updatedAt = zonedDateTimeAdapter.convertToEntityAttribute(row.getTimestamp("updatedAt"));
        builder
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .id(id);
    }

    /**
     * Binds the basic parameters of T into the statement.
     * @param t            the record
     * @param statement    the statement to have the attributes bound to.
     */
    void bindBasicParameters(T t, BoundStatement statement) {
        statement.setUUID("id", t.getIdAsUUID());
        statement.setTimestamp("createdAt", zonedDateTimeAdapter.convertToDatabaseColumn(t.getCreatedAt()));
        statement.setTimestamp("updatedAt", zonedDateTimeAdapter.convertToDatabaseColumn(t.getUpdatedAt()));
    }

    /**
     * Converts a given Row into an instance of T
     * @param row    the row to be converted
     * @return an instance of T
     */
    abstract T getFromRow(Row row);
}
