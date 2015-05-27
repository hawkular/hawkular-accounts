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

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Base entity, providing some common properties that all entities should have.
 *
 * @author Juraci Paixão Kröhling
 */
public class BaseEntity implements Serializable {
    private UUID id = UUID.randomUUID();
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt = ZonedDateTime.now();

    protected BaseEntity() {
        createdAt = ZonedDateTime.now();
        updatedAt = createdAt;
    }

    public BaseEntity(UUID id) {
        createdAt = ZonedDateTime.now();
        updatedAt = createdAt;
        if (null != id) {
            this.id = id;
        }
    }

    public BaseEntity(UUID id, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
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


}
