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
public class OrganizationMembership extends BaseEntity {

    private Organization organization;
    private Member member;
    private Role role;

    public OrganizationMembership(Organization organization, Member member, Role role) {
        this.organization = organization;
        this.member = member;
        this.role = role;
    }

    public OrganizationMembership(String id, Organization organization, Member member, Role role) {
        super(id);
        this.organization = organization;
        this.member = member;
        this.role = role;
    }

    public OrganizationMembership(UUID id, ZonedDateTime createdAt, ZonedDateTime updatedAt,
                                  Organization organization, Member member, Role role) {
        super(id, createdAt, updatedAt);
        this.organization = organization;
        this.member = member;
        this.role = role;
    }

    public Organization getOrganization() {
        return organization;
    }

    public Member getMember() {
        return member;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public static class Builder extends BaseEntity.Builder {
        private Organization organization;
        private Member member;
        private Role role;

        public Builder organization(Organization organization) {
            this.organization = organization;
            return this;
        }

        public Builder member(Member member) {
            this.member = member;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public OrganizationMembership build() {
            return new OrganizationMembership(id, createdAt, updatedAt, organization, member, role);
        }
    }
}
