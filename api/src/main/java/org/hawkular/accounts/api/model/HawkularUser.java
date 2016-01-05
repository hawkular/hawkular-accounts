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

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents a real person. The actual data for this user resides on Keycloak. For us, suffice to know the
 * user's ID on Keycloak.
 *
 * @author Juraci Paixão Kröhling
 */
public class HawkularUser extends Persona {
    private String name;
    private String email;

    // required for JSON/XML marshalling
    protected HawkularUser() {
        this(UUID.randomUUID().toString());
    }

    public HawkularUser(String id) {
        super(id);
    }

    public HawkularUser(UUID id, String name) {
        super(id);
        this.name = name;
    }

    public HawkularUser(UUID id, String name, String email) {
        super(id);
        this.name = name;
    }

    public HawkularUser(UUID id, ZonedDateTime createdAt, ZonedDateTime updatedAt,
                        String name) {
        super(id, createdAt, updatedAt);
        this.name = name;
    }

    public HawkularUser(UUID id, ZonedDateTime createdAt, ZonedDateTime updatedAt,
                        String name, String email) {
        super(id, createdAt, updatedAt);
        this.name = name;
        this.email = email;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public static class Builder extends BaseEntity.Builder {
        private String name;
        private String email;
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public HawkularUser build() {
            return new HawkularUser(id, createdAt, updatedAt, name, email);
        }
    }
}
