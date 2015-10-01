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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * @author Juraci Paixão Kröhling
 */
@Entity
public class Invitation extends BaseEntity {

    private String email;
    private ZonedDateTime acceptedAt = null;
    private ZonedDateTime dispatchedAt = null;

    @Column(unique = true)
    private final String token = UUID.randomUUID().toString();

    @ManyToOne
    private HawkularUser invitedBy;

    @ManyToOne
    private HawkularUser acceptedBy;

    @ManyToOne
    private Organization organization;

    @ManyToOne
    private Role role;

    protected Invitation() {
    }

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

    public String getToken() {
        return token;
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
}
