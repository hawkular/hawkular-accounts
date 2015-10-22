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

import java.util.UUID;

/**
 * Represents a Role in the system. Roles can be, for instance, "SuperUser", "Monitor", "Auditor" and so on.
 *
 * @author Juraci Paixão Kröhling
 */
public class Role extends BaseEntity {
    private String name;
    private String description;

    public Role(String name, String description) {
        this(UUID.randomUUID(), name, description);
    }

    public Role(String id, String name, String description) {
        this(UUID.fromString(id), name, description);
    }

    public Role(UUID id, String name, String description) {
        super(id);

        if (null == name) {
            throw new IllegalStateException("A role name is required to build a role.");
        }

        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Role{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", base='" + super.toString() + '\'' +
                '}';
    }

    public static class Builder extends BaseEntity.Builder {
        private String name;
        private String description;

        public Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Role build() {
            return new Role(this.id, this.name, this.description);
        }
    }
}
