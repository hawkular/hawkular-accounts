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

import org.hawkular.accounts.api.BaseEntityManagerEnabledTest;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Invitation;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationMembership;
import org.hawkular.accounts.api.model.Role;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Juraci Paixão Kröhling
 */
public class InvitationServiceImplTest extends BaseEntityManagerEnabledTest {
    OrganizationMembershipServiceImpl membershipService = new OrganizationMembershipServiceImpl();
    InvitationServiceImpl invitationService = new InvitationServiceImpl();

    @Before
    public void prepare() {
        invitationService.em = entityManager;
        membershipService.em = entityManager;
    }

    @Test
    public void inviteUserToOrganization() {
        entityManager.getTransaction().begin();
        HawkularUser jdoe = new HawkularUser(UUID.randomUUID().toString());
        HawkularUser jsmith = new HawkularUser(UUID.randomUUID().toString());
        Organization acme = new Organization(jdoe);
        Role monitor = new Role("Monitor", "can monitor things");
        entityManager.persist(jdoe);
        entityManager.persist(jsmith);
        entityManager.persist(acme);
        entityManager.persist(monitor);
        entityManager.getTransaction().commit();

        entityManager.clear();
        entityManager.getTransaction().begin();
        Invitation invitation = invitationService.create(new Invitation("email", jdoe, acme, monitor));
        invitationService.accept(invitation, jsmith);
        entityManager.getTransaction().commit();

        entityManager.clear();
        entityManager.getTransaction().begin();
        List<OrganizationMembership> memberships = membershipService.getPersonaMembershipsForOrganization(jsmith, acme);
        assertEquals("Should have one membership", 1, memberships.size());
        assertEquals("Membership should be as Monitor", "Monitor", memberships.get(0).getRole().getName());
        entityManager.getTransaction().commit();
    }

}
