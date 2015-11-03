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
package org.hawkular.accounts.api.internal.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.UUID;

import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Invitation;
import org.hawkular.accounts.api.model.Organization;
import org.junit.Test;

/**
 * @author Juraci Paixão Kröhling
 */
public class OrganizationServiceImplTest extends SessionEnabledTest {
    @Test
    public void listOrganizationsForUserNotBelongingToAnyOrganization() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());

        List<Organization> organizations = organizationService.getOrganizationsForPersona(jdoe);
        assertEquals("The persona should not belong to any organizations", 0, organizations.size());
    }

    @Test
    public void listOrganizationsForUserBelongingToOrganization() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization acme = organizationService.createOrganization("Acme, Inc", "Acme, Inc", jdoe);

        List<Organization> memberships = organizationService.getOrganizationsForPersona(jdoe);
        assertEquals("There should be one membership for this persona", 1, memberships.size());
    }

    @Test
    public void listOrganizationsForSoleOrganization() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization acme = organizationService.createOrganization("Acme, Inc", "Acme, Inc", jdoe);

        List<Organization> memberships = organizationService.getOrganizationsForPersona(acme);
        assertEquals("There should be no memberships for this organization", 0, memberships.size());
    }

    @Test
    public void listMembershipsForOrganizationBelongingToOrganization() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization acme = organizationService.createOrganization("Acme, Inc", "Acme, Inc", jdoe);
        Organization itDepartment = organizationService.createOrganization("IT Dep", "IT Dep", acme);

        List<Organization> memberships = organizationService.getOrganizationsForPersona(acme);
        assertEquals("Acme is super persona of IT department", 1, memberships.size());
    }

    @Test
    public void removeOrganization() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization organization = organizationService.createOrganization("Acme, Inc", "Acme, Inc", jdoe);

        organizationService.deleteOrganization(organization);

        assertNull("Organization should have been removed", organizationService.get(organization.getId()));
    }

    @Test
    public void removeOrganizationWithPendingInvitations() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization organization = organizationService.createOrganization("Acme, Inc", "Acme, Inc", jdoe);
        invitationService.create("", jdoe, organization, administrator);
        organizationService.deleteOrganization(organization);

        assertNull("Organization should have been removed", organizationService.get(organization.getId()));
    }

    @Test
    public void removeOrganizationWithInvitations() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization organization = organizationService.createOrganization("Acme, Inc", "Acme, Inc", jdoe);
        Invitation invitation = invitationService.create("", jdoe, organization, administrator);
        invitationService.accept(invitation, jsmith);

        organizationService.deleteOrganization(organization);

        assertNull("Organization should have been removed", organizationService.get(organization.getId()));
    }
}
