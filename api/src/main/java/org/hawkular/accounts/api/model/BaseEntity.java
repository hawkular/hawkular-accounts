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
package org.hawkular.accounts.api.model;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Base entity, providing some common properties that all entities should have.
 *
 * @author Juraci Paixão Kröhling
 */
public class BaseEntity implements Serializable {

    // as of now, this is relevant *only* for the Resource class, but we might find it useful to have it here
    private static final Pattern UUID_PATTERN =
            Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");

    private UUID id = UUID.randomUUID();
    private ZonedDateTime createdAt = ZonedDateTime.now();
    private ZonedDateTime updatedAt = ZonedDateTime.now();

    public BaseEntity() {
    }

    /**
     * @deprecated Use the BaseEntity(UUID) instead.
     * @param id    the ID as String to be parsed as UUID.
     */
    public BaseEntity(String id) {
        if (null != id) {
            if (!UUID_PATTERN.matcher(id).matches()) {
                // not an UUID, so, let's convert it to UUID
                this.id = UUID.nameUUIDFromBytes(id.getBytes());
            } else {
                this.id = UUID.fromString(id);
            }
        }
    }

    public BaseEntity(UUID id) {
        if (null != id) {
            this.id = id;
        }
    }

    public BaseEntity(UUID id, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        if (null != id) {
            this.id = id;
        }

        if (null != createdAt) {
            this.createdAt = createdAt;
        }

        if (null != updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    @JsonIgnore
    public UUID getIdAsUUID() {
        return id;
    }

    /**
     * @deprecated from Accounts 2.x, this will return an UUID. Use getIdAsUUID().
     * @return the ID as String
     */
    public String getId() {
        return id.toString();
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt() {
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public String toString() {
        return "BaseEntity{" + "id=" + id + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BaseEntity other = (BaseEntity) obj;
        return Objects.equals(this.id, other.id);
    }

    /**
     * Abstract builder class, providing base methods for the base properties.
     */
    public abstract static class Builder {
        UUID id = UUID.randomUUID();
        ZonedDateTime createdAt = ZonedDateTime.now();
        ZonedDateTime updatedAt = ZonedDateTime.now();

        public Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder createdAt(ZonedDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(ZonedDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
    }
}
