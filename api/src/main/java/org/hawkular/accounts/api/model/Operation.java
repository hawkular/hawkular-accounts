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
package org.hawkular.accounts.api.model;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents an operation, that can be for instance "metric-create".
 *
 * @author Juraci Paixão Kröhling
 */
public class Operation extends BaseEntity {
    private String name;

    public Operation(String name) {
        this.name = name;
    }

    public Operation(String id, String name) {
        super(id);
        this.name = name;
    }

    public Operation(UUID id, ZonedDateTime createdAt, ZonedDateTime updatedAt,
                     String name) {
        super(id, createdAt, updatedAt);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Operation{" +
                "name='" + name + '\'' +
                ", base='" + super.toString() + '\'' +
                '}';
    }

    public static class Builder extends BaseEntity.Builder {
        private String name;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Operation build() {
            return new Operation(id, createdAt, updatedAt, name);
        }
    }
}
