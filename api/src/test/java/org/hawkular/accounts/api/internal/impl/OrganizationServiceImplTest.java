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

import org.hawkular.accounts.api.BaseEntityManagerEnabledTest;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationMembership;
import org.hawkular.accounts.api.model.Role;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author Juraci Paixão Kröhling
 */
public class OrganizationServiceImplTest extends BaseEntityManagerEnabledTest {

    private OrganizationServiceImpl organizationService = new OrganizationServiceImpl();
    private OrganizationMembershipServiceImpl membershipService = new OrganizationMembershipServiceImpl();

    @Before
    public void setup() {
        membershipService.em = entityManager;
        organizationService.em = entityManager;
        organizationService.membershipService = membershipService;
    }

    @Test
    public void listOrganizationsForUserNotBelongingToAnyOrganization() {
        entityManager.getTransaction().begin();
        HawkularUser jdoe = new HawkularUser(UUID.randomUUID().toString());
        entityManager.persist(jdoe);
        entityManager.getTransaction().commit();

        List<Organization> organizations = organizationService.getOrganizationsForPersona(jdoe);
        assertEquals("The persona should not belong to any organizations", 0, organizations.size());
    }

    @Test
    public void listOrganizationsForUserBelongingToOrganization() {
        entityManager.getTransaction().begin();
        HawkularUser jdoe = new HawkularUser(UUID.randomUUID().toString());
        Organization acme = new Organization(jdoe);
        Role role = new Role("Super User", "can do anything");
        OrganizationMembership membership = new OrganizationMembership(acme, jdoe, role);
        entityManager.persist(jdoe);
        entityManager.persist(acme);
        entityManager.persist(role);
        entityManager.persist(membership);
        entityManager.getTransaction().commit();

        List<Organization> memberships = organizationService.getOrganizationsForPersona(jdoe);
        assertEquals("There should be one membership for this persona", 1, memberships.size());
    }

    @Test
    public void listOrganizationsForSoleOrganization() {
        entityManager.getTransaction().begin();
        HawkularUser jdoe = new HawkularUser(UUID.randomUUID().toString());
        Organization acme = new Organization(jdoe);
        Role role = new Role("Super User", "can do anything");
        OrganizationMembership membership = new OrganizationMembership(acme, jdoe, role);
        entityManager.persist(jdoe);
        entityManager.persist(acme);
        entityManager.persist(role);
        entityManager.persist(membership);
        entityManager.getTransaction().commit();

        List<Organization> memberships = organizationService.getOrganizationsForPersona(acme);
        assertEquals("There should be no memberships for this organization", 0, memberships.size());
    }

    @Test
    public void listMembershipsForOrganizationBelongingToOrganization() {
        entityManager.getTransaction().begin();
        HawkularUser jdoe = new HawkularUser(UUID.randomUUID().toString());
        Organization acme = new Organization(jdoe);
        Organization itDepartment = new Organization(acme);
        Role superUser = new Role("Super User", "can do anything");
        Role administrator = new Role("Administrator", "not quite everything");
        OrganizationMembership membershipAcme = new OrganizationMembership(acme, jdoe, superUser);
        OrganizationMembership membershipItDepartment = new OrganizationMembership(itDepartment, acme, superUser);

        entityManager.persist(jdoe);
        entityManager.persist(acme);
        entityManager.persist(itDepartment);
        entityManager.persist(superUser);
        entityManager.persist(administrator);
        entityManager.persist(membershipAcme);
        entityManager.persist(membershipItDepartment);
        entityManager.getTransaction().commit();

        List<Organization> memberships = organizationService.getOrganizationsForPersona(acme);
        assertEquals("Acme is super persona of IT department", 1, memberships.size());
    }

}
