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
 * Represents an non-user model that can own resources. It has itself an owner and may contain zero or more members.
 * The owner is not included in the member's list, but is assumed to be a "special" member. In other words: even if
 * the owner is not explicitly on the members list, it should be counted as being a member.
 *
 * @author Juraci Paixão Kröhling
 */
public class Organization extends Persona {
    private Persona owner;
    private String name;
    private String description;
    private Visibility visibility;

    public Organization(String id, Persona owner) {
        super(id);
        this.owner = owner;
    }

    public Organization(Persona owner) {
        super(UUID.randomUUID().toString());
        this.owner = owner;
    }

    public Organization(UUID id, ZonedDateTime createdAt, ZonedDateTime updatedAt,
                        Persona owner, String name, String description) {
        this(id, createdAt, updatedAt, owner, name, description, Visibility.PRIVATE);
    }

    public Organization(UUID id, ZonedDateTime createdAt, ZonedDateTime updatedAt,
                        Persona owner, String name, String description, Visibility visibility) {
        super(id, createdAt, updatedAt);
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.visibility = visibility;
    }

    public Persona getOwner() {
        return owner;
    }

    public void setOwner(Persona owner) {
        this.owner = owner;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public static class Builder extends BaseEntity.Builder {
        private Persona owner;
        private String name;
        private String description;
        private Visibility visibility;

        public Builder owner(Persona owner) {
            this.owner = owner;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder visibility(String visibility) {
            this.visibility = Visibility.valueOf(visibility);
            return this;
        }

        public Organization build() {
            return new Organization(id, createdAt, updatedAt, owner, name, description, visibility);
        }
    }
}
