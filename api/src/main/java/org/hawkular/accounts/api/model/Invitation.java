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
 * @author Juraci Paixão Kröhling
 */
public class Invitation extends BaseEntity {

    private String email;
    private ZonedDateTime acceptedAt = null;
    private ZonedDateTime dispatchedAt = null;
    private HawkularUser invitedBy;
    private HawkularUser acceptedBy;
    private Organization organization;
    private Role role;

    public Invitation(String email, HawkularUser invitedBy, Organization organization, Role role) {
        this.email = email;
        this.invitedBy = invitedBy;
        this.organization = organization;
        this.role = role;
    }

    public Invitation(String id, String email, HawkularUser invitedBy, Organization organization, Role role) {
        super(id);
        this.email = email;
        this.invitedBy = invitedBy;
        this.organization = organization;
        this.role = role;
    }

    public Invitation(UUID id, ZonedDateTime createdAt, ZonedDateTime updatedAt, String email,
                      ZonedDateTime acceptedAt, ZonedDateTime dispatchedAt,
                      HawkularUser invitedBy, HawkularUser acceptedBy,
                      Organization organization, Role role) {
        super(id, createdAt, updatedAt);
        this.email = email;
        this.acceptedAt = acceptedAt;
        this.dispatchedAt = dispatchedAt;
        this.invitedBy = invitedBy;
        this.acceptedBy = acceptedBy;
        this.organization = organization;
        this.role = role;
    }

    public String getToken() {
        return getId();
    }

    public HawkularUser getInvitedBy() {
        return invitedBy;
    }

    public Organization getOrganization() {
        return organization;
    }

    public Role getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public ZonedDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(ZonedDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public ZonedDateTime getDispatchedAt() {
        return dispatchedAt;
    }

    public void setDispatchedAt(ZonedDateTime dispatchedAt) {
        this.dispatchedAt = dispatchedAt;
    }

    /**
     * Sets the invitation as accepted as of 'now'.
     */
    public void setAccepted() {
        this.acceptedAt = ZonedDateTime.now();
    }

    /**
     * Sets the invitation as accepted as of 'now'.
     */
    public void setDispatched() {
        this.dispatchedAt = ZonedDateTime.now();
    }

    public HawkularUser getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(HawkularUser acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public static class Builder extends BaseEntity.Builder {
        private String email;
        private ZonedDateTime acceptedAt;
        private ZonedDateTime dispatchedAt;
        private HawkularUser invitedBy;
        private HawkularUser acceptedBy;
        private Organization organization;
        private Role role;

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder acceptedAt(ZonedDateTime acceptedAt) {
            this.acceptedAt = acceptedAt;
            return this;
        }

        public Builder dispatchedAt(ZonedDateTime dispatchedAt) {
            this.dispatchedAt = dispatchedAt;
            return this;
        }

        public Builder invitedBy(HawkularUser invitedBy) {
            this.invitedBy = invitedBy;
            return this;
        }

        public Builder acceptedBy(HawkularUser acceptedBy) {
            this.acceptedBy = acceptedBy;
            return this;
        }

        public Builder organization(Organization organization) {
            this.organization = organization;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Invitation build() {
            return new Invitation(id, createdAt, updatedAt, email, acceptedAt, dispatchedAt, invitedBy, acceptedBy,
                    organization, role);
        }
    }
}
