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

import java.util.List;
import java.util.UUID;

import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Invitation;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationMembership;
import org.junit.Test;

/**
 * @author Juraci Paixão Kröhling
 */
public class OrganizationMembershipServiceImplTest extends SessionEnabledTest {
    @Test
    public void listMembershipsForUserNotBelongingToAnyOrganization() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());

        List<OrganizationMembership> memberships = membershipService.getMembershipsForPersona(jdoe);
        assertEquals("The persona should not belong to any organizations", 0, memberships.size());
    }

    @Test
    public void listMembershipsForUserBelongingToOrganization() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        organizationService.createOrganization("", "", jdoe);

        List<OrganizationMembership> memberships = membershipService.getMembershipsForPersona(jdoe);
        assertEquals("There should be one membership for this persona", 1, memberships.size());
    }

    @Test
    public void listMembershipsForSoleOrganization() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization acme = organizationService.createOrganization("", "", jdoe);

        List<OrganizationMembership> memberships = membershipService.getMembershipsForPersona(acme);
        assertEquals("There should be no memberships for this organization", 0, memberships.size());
    }

    @Test
    public void listMembershipsForOrganizationBelongingToOrganization() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization acme = organizationService.createOrganization("", "", jdoe);
        Organization itDepartment = organizationService.createOrganization("", "", acme);

        List<OrganizationMembership> memberships = membershipService.getMembershipsForPersona(acme);
        assertEquals("Acme is super persona of IT department", 1, memberships.size());
    }

    @Test
    public void changeRole() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization acme = organizationService.createOrganization("", "", jdoe);

        Invitation invitation = invitationService.create("", jdoe, acme, monitor);
        invitationService.accept(invitation, jsmith);

        OrganizationMembership jsmithAtAcme = membershipService.getMembershipsForPersona(jsmith).get(0);
        membershipService.changeRole(jsmithAtAcme, superUser);

        List<OrganizationMembership> memberships = membershipService.getPersonaMembershipsForOrganization(jsmith, acme);
        assertEquals("jsmith should be only super user at acme at this point", 1, memberships.size());
        assertEquals("jsmith should be only super user at acme at this point", "SuperUser",
                memberships.get(0).getRole().getName());

        membershipService.changeRole(jsmithAtAcme, monitor);

        memberships = membershipService.getPersonaMembershipsForOrganization(jsmith, acme);
        assertEquals("jsmith should be only monitor at acme at this point", 1, memberships.size());
        assertEquals("jsmith should be only monitor at acme at this point", "Monitor",
                memberships.get(0).getRole().getName());
    }
}
