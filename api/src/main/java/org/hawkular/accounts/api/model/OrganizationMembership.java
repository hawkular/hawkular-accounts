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

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * @author Juraci Paixão Kröhling
 */
@Entity
public class OrganizationMembership extends BaseEntity {

    @ManyToOne
    private Organization organization;

    @ManyToOne
    private Member member;

    @ManyToOne
    private Role role;

    protected OrganizationMembership() { // JPA happy
    }

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
}
