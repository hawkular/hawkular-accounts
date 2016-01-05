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
package org.hawkular.accounts.api.internal.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Invitation;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationJoinRequest;
import org.hawkular.accounts.api.model.Visibility;
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
        Organization acme = organizationService.createOrganization(UUID.randomUUID().toString(), "", jdoe);

        List<Organization> memberships = organizationService.getOrganizationsForPersona(jdoe);
        assertEquals("There should be one membership for this persona", 1, memberships.size());
    }

    @Test
    public void listOrganizationsForSoleOrganization() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization acme = organizationService.createOrganization(UUID.randomUUID().toString(), "", jdoe);

        List<Organization> memberships = organizationService.getOrganizationsForPersona(acme);
        assertEquals("There should be no memberships for this organization", 0, memberships.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotCreateOrganizationWithExistingName() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        organizationService.createOrganization("cannotCreateOrganizationWithExistingName", "", jdoe);
        organizationService.createOrganization("cannotCreateOrganizationWithExistingName", "", jdoe);
    }

    @Test
    public void createOrganizationWithDefaultVisibility() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization organization = organizationService
                .createOrganization("createOrganizationWithDefaultVisibility", "", jdoe);
        assertEquals("The default visibility should be PRIVATE", Visibility.PRIVATE, organization.getVisibility());

        organization = organizationService.getById(organization.getIdAsUUID());
        assertEquals("The default visibility should be PRIVATE", Visibility.PRIVATE, organization.getVisibility());
    }

    @Test
    public void createOrganizationWithPrivateVisibility() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization organization = organizationService
                .createOrganization("createOrganizationWithPrivateVisibility", "", Visibility.PRIVATE, jdoe);
        assertEquals("The default visibility should be PRIVATE", Visibility.PRIVATE, organization.getVisibility());

        organization = organizationService.getById(organization.getIdAsUUID());
        assertEquals("The default visibility should be PRIVATE", Visibility.PRIVATE, organization.getVisibility());
    }

    @Test
    public void createOrganizationWithApplyVisibility() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization organization = organizationService
                .createOrganization("createOrganizationWithApplyVisibility", "", Visibility.APPLY, jdoe);
        assertEquals("The default visibility should be PRIVATE", Visibility.APPLY, organization.getVisibility());

        organization = organizationService.getById(organization.getIdAsUUID());
        assertEquals("The default visibility should be PRIVATE", Visibility.APPLY, organization.getVisibility());
    }

    @Test
    public void listMembershipsForOrganizationBelongingToOrganization() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization acme = organizationService.createOrganization(UUID.randomUUID().toString(), "", jdoe);
        Organization itDepartment = organizationService.createOrganization("IT Dep", "IT Dep", acme);

        List<Organization> memberships = organizationService.getOrganizationsForPersona(acme);
        assertEquals("Acme is super persona of IT department", 1, memberships.size());
    }

    @Test
    public void removeOrganization() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization organization = organizationService.createOrganization(UUID.randomUUID().toString(), "", jdoe);

        organizationService.deleteOrganization(organization);

        assertNull("Organization should have been removed", organizationService.get(organization.getId()));
    }

    @Test
    public void removeOrganizationWithPendingInvitations() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization organization = organizationService.createOrganization(UUID.randomUUID().toString(), "", jdoe);
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

    @Test
    public void filteredOrganizationsToJoin() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization organization1 = organizationService.createOrganization("Org1", "", Visibility.APPLY, jdoe);
        Organization organization2 = organizationService.createOrganization("Org2", "", Visibility.APPLY, jdoe);
        Organization organization3 = organizationService.createOrganization("Org3", "", Visibility.APPLY, jdoe);
        Organization organization4 = organizationService.createOrganization("Org4", "", Visibility.APPLY, jdoe);
        Organization organization5 = organizationService.createOrganization("Org5", "", Visibility.PRIVATE, jdoe);

        // this organization should be filtered out, as the user now belongs to the organization
        OrganizationJoinRequest request1 = joinRequestService.create(organization1, jsmith);
        joinRequestService.accept(request1, superUser);

        // this organization should NOT be filtered out, as the user can apply again
        OrganizationJoinRequest request2 = joinRequestService.create(organization2, jsmith);
        joinRequestService.reject(request2);

        // this organization should be filtered out, as the request is still pending
        OrganizationJoinRequest request3 = joinRequestService.create(organization3, jsmith);

        // there should be: org2 and org4 on the list (and others from other tests)
        List<Organization> organizationsToJoin = organizationService.getFilteredOrganizationsToJoin(jsmith);
        assertTrue("The organization 1 should NOT be available to join", !organizationsToJoin.contains(organization1));
        assertTrue("The organization 2 should be available to join", organizationsToJoin.contains(organization2));
        assertTrue("The organization 3 should NOT be available to join", !organizationsToJoin.contains(organization3));
        assertTrue("The organization 4 should be available to join", organizationsToJoin.contains(organization4));
        assertTrue("The organization 5 should not be available to join", !organizationsToJoin.contains(organization5));
    }
}
