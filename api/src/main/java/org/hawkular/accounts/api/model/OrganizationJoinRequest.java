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
 * @author Juraci Paixão Kröhling
 */
public class OrganizationJoinRequest extends BaseEntity {
    private Organization organization;
    private Persona persona;
    private JoinRequestStatus status;

    public OrganizationJoinRequest(Organization organization, Persona persona,
                                   JoinRequestStatus status) {
        this.organization = organization;
        this.persona = persona;
        this.status = status;
    }

    public OrganizationJoinRequest(UUID id, ZonedDateTime createdAt, ZonedDateTime updatedAt,
                                   Organization organization, Persona persona,
                                   JoinRequestStatus status) {
        super(id, createdAt, updatedAt);
        this.organization = organization;
        this.persona = persona;
        this.status = status;
    }

    public Organization getOrganization() {
        return organization;
    }

    public Persona getPersona() {
        return persona;
    }

    public JoinRequestStatus getStatus() {
        return status;
    }

    public void setStatus(JoinRequestStatus status) {
        this.status = status;
    }

    public static class Builder extends BaseEntity.Builder {
        private Organization organization;
        private Persona persona;
        private JoinRequestStatus status;

        public Builder organization(Organization organization) {
            this.organization = organization;
            return this;
        }

        public Builder persona(Persona persona) {
            this.persona = persona;
            return this;
        }

        public Builder status(JoinRequestStatus status) {
            this.status = status;
            return this;
        }

        public OrganizationJoinRequest build() {
            return new OrganizationJoinRequest(id, createdAt, updatedAt, organization, persona, status);
        }
    }
}
