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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.JoinRequestStatus;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationJoinRequest;
import org.hawkular.accounts.api.model.OrganizationMembership;
import org.hawkular.accounts.api.model.Visibility;
import org.junit.Test;

/**
 * @author Juraci Paixão Kröhling
 */
@SuppressWarnings("Duplicates")
public class OrganizationJoinRequestServiceImplTest extends SessionEnabledTest {

    /**
     * jsmith tries to apply for an organization, but the organization is private (ie: users can only be invited,
     * users cannot apply)
     */
    @Test(expected = IllegalArgumentException.class)
    public void createJoinRequestOnPrivateOrganization() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization acme = organizationService.createOrganization(
                UUID.randomUUID().toString(),
                "",
                Visibility.PRIVATE,
                jdoe
        );

        joinRequestService.create(acme, jsmith);
    }

    @Test
    public void createJoinRequestOnOrganization() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization acme = organizationService.createOrganization(
                UUID.randomUUID().toString(),
                "",
                Visibility.APPLY,
                jdoe
        );

        joinRequestService.create(acme, jsmith);
    }

    @Test
    public void acceptJoinRequest() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization acme = organizationService.createOrganization(
                UUID.randomUUID().toString(),
                "",
                Visibility.APPLY,
                jdoe
        );

        OrganizationJoinRequest request = joinRequestService.create(acme, jsmith);
        joinRequestService.accept(request, monitor);

        List<OrganizationMembership> memberships = membershipService.getPersonaMembershipsForOrganization(jsmith, acme);
        assertEquals("Should have one membership", 1, memberships.size());
        assertEquals("Membership should be as Monitor", "Monitor", memberships.get(0).getRole().getName());

        request = joinRequestService.getById(request.getIdAsUUID());
        assertEquals("Request should be marked as accepted", JoinRequestStatus.ACCEPTED, request.getStatus());
    }

    @Test(expected = IllegalStateException.class)
    public void acceptRejectedJoinRequest() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization acme = organizationService.createOrganization(
                UUID.randomUUID().toString(),
                "",
                Visibility.APPLY,
                jdoe
        );

        OrganizationJoinRequest request = joinRequestService.create(acme, jsmith);
        joinRequestService.reject(request);
        joinRequestService.accept(request, monitor);
    }

    @Test(expected = IllegalStateException.class)
    public void rejectAcceptedJoinRequest() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization acme = organizationService.createOrganization(
                UUID.randomUUID().toString(),
                "",
                Visibility.APPLY,
                jdoe
        );

        OrganizationJoinRequest request = joinRequestService.create(acme, jsmith);
        joinRequestService.accept(request, monitor);
        joinRequestService.reject(request);
    }

    @Test
    public void rejectJoinRequest() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization acme = organizationService.createOrganization(
                UUID.randomUUID().toString(),
                "",
                Visibility.APPLY,
                jdoe
        );

        OrganizationJoinRequest request = joinRequestService.create(acme, jsmith);
        joinRequestService.reject(request);

        List<OrganizationMembership> memberships = membershipService.getPersonaMembershipsForOrganization(jsmith, acme);
        assertEquals("Should have no memberships", 0, memberships.size());

        request = joinRequestService.getById(request.getIdAsUUID());
        assertEquals("Request should be marked as rejected", JoinRequestStatus.REJECTED, request.getStatus());
    }

    @Test
    public void removeOrganizationWithPendingRequests() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization acme = organizationService.createOrganization(
                UUID.randomUUID().toString(),
                "",
                Visibility.APPLY,
                jdoe
        );

        joinRequestService.create(acme, jsmith);

        organizationService.deleteOrganization(acme);
        acme = organizationService.getById(acme.getIdAsUUID());
        assertNull("Organization should have been removed", acme);
    }

    @Test
    public void removeOrganizationWithAcceptedRequests() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization acme = organizationService.createOrganization(
                UUID.randomUUID().toString(),
                "",
                Visibility.APPLY,
                jdoe
        );

        OrganizationJoinRequest request = joinRequestService.create(acme, jsmith);
        joinRequestService.accept(request, monitor);

        organizationService.deleteOrganization(acme);
        acme = organizationService.getById(acme.getIdAsUUID());
        assertNull("Organization should have been removed", acme);
    }

    @Test
    public void listPendingRequests() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization acme = organizationService.createOrganization(
                UUID.randomUUID().toString(),
                "",
                Visibility.APPLY,
                jdoe
        );

        OrganizationJoinRequest request = joinRequestService.create(acme, jsmith);
        OrganizationJoinRequest foundRequest = joinRequestService
                .getPendingRequestsForOrganization(acme)
                .stream()
                .findFirst()
                .get();

        assertEquals("The request should have been found", request, foundRequest);
    }

    @Test
    public void listAllRequests() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization acme = organizationService.createOrganization(
                UUID.randomUUID().toString(),
                "",
                Visibility.APPLY,
                jdoe
        );

        OrganizationJoinRequest request = joinRequestService.create(acme, jsmith);
        joinRequestService.reject(request);

        OrganizationJoinRequest request2 = joinRequestService.create(acme, jsmith);
        joinRequestService.accept(request2, monitor);

        OrganizationJoinRequest request3 = joinRequestService.create(acme, jsmith);
        joinRequestService.remove(request3);

        List<OrganizationJoinRequest> foundRequests = joinRequestService.getAllRequestsForOrganization(acme);
        assertEquals("There should have been 2 requests found", 2, foundRequests.size());
        assertTrue("First request should have been found", foundRequests.contains(request));
        assertTrue("Second request should have been found", foundRequests.contains(request2));
        assertFalse("Third request should have not been found", foundRequests.contains(request3));
    }

}
