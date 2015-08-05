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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.UUID;

import org.hawkular.accounts.api.BaseEntityManagerEnabledTest;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationMembership;
import org.hawkular.accounts.api.model.PersonaResourceRole;
import org.hawkular.accounts.api.model.Resource;
import org.hawkular.accounts.api.model.Role;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Juraci Paixão Kröhling
 */
public class PersonaServiceImplTest extends BaseEntityManagerEnabledTest {

    PersonaServiceImpl personaService = new PersonaServiceImpl();
    OrganizationMembershipServiceImpl membershipService = new OrganizationMembershipServiceImpl();
    OrganizationServiceImpl organizationService = new OrganizationServiceImpl();

    @Before
    public void setup() {
        membershipService.em = entityManager;

        organizationService.em = entityManager;
        organizationService.membershipService = membershipService;

        personaService.em = entityManager;
        personaService.membershipService = membershipService;
        personaService.organizationService = organizationService;
    }

    @Test
    public void existingIdIsFound() {
        entityManager.getTransaction().begin();
        String id = UUID.randomUUID().toString();
        entityManager.persist(new HawkularUser(id));
        entityManager.getTransaction().commit();

        assertNotNull(personaService.get(id));
    }

    @Test
    public void nonExistingIdReturnsNull() {
        entityManager.getTransaction().begin();
        String id = UUID.randomUUID().toString();
        entityManager.persist(new HawkularUser(id));
        entityManager.getTransaction().commit();

        assertNull(personaService.get("non-existing-id"));
    }

    @Test
    public void userHasNoRoleOnResource() {
        entityManager.getTransaction().begin();
        HawkularUser user = new HawkularUser(UUID.randomUUID().toString());
        Resource resource = new Resource(user);
        entityManager.persist(user);
        entityManager.persist(resource);
        entityManager.getTransaction().commit();

        assertEquals("User should have no roles on resource", 0, personaService.getEffectiveRolesForResource(user,
                resource).size());
    }

    @Test
    public void userHaveRoleOnResource() {
        entityManager.getTransaction().begin();
        HawkularUser user = new HawkularUser(UUID.randomUUID().toString());
        Resource resource = new Resource(user);
        Role superUser = new Role("SuperUser", "can do anything");
        Role administrator = new Role("Administrator", "not quite everything");
        PersonaResourceRole personaResourceRole = new PersonaResourceRole(user, superUser, resource);
        entityManager.persist(user);
        entityManager.persist(resource);
        entityManager.persist(superUser);
        entityManager.persist(administrator);
        entityManager.persist(personaResourceRole);
        entityManager.getTransaction().commit();

        Set<Role> rolesForResource = personaService.getEffectiveRolesForResource(user, resource);

        assertEquals("User should have SuperUser role on resource", 1, rolesForResource.size());
        Role role = rolesForResource.stream().findFirst().get();
        assertEquals("SuperUser", role.getName());
    }

    @Test
    public void onlyRolesOnResourceShouldCount() {
        entityManager.getTransaction().begin();
        HawkularUser user = new HawkularUser(UUID.randomUUID().toString());
        Resource resource = new Resource(user);
        Resource resource2 = new Resource(user);
        Role superUser = new Role("SuperUser", "can do anything");
        Role administrator = new Role("Administrator", "not quite everything");
        PersonaResourceRole personaResourceRole = new PersonaResourceRole(user, superUser, resource);
        PersonaResourceRole personaResourceRole2 = new PersonaResourceRole(user, superUser, resource2);
        entityManager.persist(user);
        entityManager.persist(resource);
        entityManager.persist(resource2);
        entityManager.persist(superUser);
        entityManager.persist(administrator);
        entityManager.persist(personaResourceRole);
        entityManager.persist(personaResourceRole2);
        entityManager.getTransaction().commit();

        Set<Role> rolesForResource = personaService.getEffectiveRolesForResource(user, resource);

        assertEquals("User should have SuperUser role on resource", 1, rolesForResource.size());
        Role role = rolesForResource.stream().findFirst().get();
        assertEquals("SuperUser", role.getName());
    }

    @Test
    public void userCanHaveMoreThanOneRoleOnResource() {
        entityManager.getTransaction().begin();
        HawkularUser user = new HawkularUser(UUID.randomUUID().toString());
        Resource resource = new Resource(user);
        Role superUser = new Role("SuperUser", "can do anything");
        Role administrator = new Role("Administrator", "not quite everything");
        PersonaResourceRole personaResourceRole = new PersonaResourceRole(user, superUser, resource);
        PersonaResourceRole personaResourceRole2 = new PersonaResourceRole(user, administrator, resource);
        entityManager.persist(user);
        entityManager.persist(resource);
        entityManager.persist(superUser);
        entityManager.persist(administrator);
        entityManager.persist(personaResourceRole);
        entityManager.persist(personaResourceRole2);
        entityManager.getTransaction().commit();

        Set<Role> rolesForResource = personaService.getEffectiveRolesForResource(user, resource);

        assertEquals("User should have SuperUser and Admin role on resource", 2, rolesForResource.size());
        boolean adminFound = false;
        boolean superUserFound = false;
        for (Role role : rolesForResource) {
            if (role.getName().equals("SuperUser")) {
                superUserFound = true;
            }
            if (role.getName().equals("Administrator")) {
                adminFound = true;
            }
        }

        assertTrue("User should have had super persona roles", superUserFound);
        assertTrue("User should have had admin roles", adminFound);
    }

    @Test
    public void directPermissionOnResourceIgnoresOrganizationRolesOnSameResource() {
        entityManager.getTransaction().begin();
        HawkularUser user = new HawkularUser(UUID.randomUUID().toString());
        Organization organization = new Organization(user);
        Resource resource = new Resource(organization);
        Role superUser = new Role("SuperUser", "can do anything");
        Role administrator = new Role("Administrator", "not quite everything");
        PersonaResourceRole personaResourceRole = new PersonaResourceRole(organization, superUser, resource);
        PersonaResourceRole personaResourceRole2 = new PersonaResourceRole(user, administrator, resource);
        entityManager.persist(user);
        entityManager.persist(organization);
        entityManager.persist(resource);
        entityManager.persist(superUser);
        entityManager.persist(administrator);
        entityManager.persist(personaResourceRole);
        entityManager.persist(personaResourceRole2);
        entityManager.getTransaction().commit();

        Set<Role> rolesForResource = personaService.getEffectiveRolesForResource(user, resource);

        assertEquals("User should have Administrator role on resource", 1, rolesForResource.size());
        Role role = rolesForResource.stream().findFirst().get();
        assertEquals("Administrator", role.getName());
    }

    @Test
    public void organizationTreePutsUserWithTwoRolesInTwoOrganizations() {
        entityManager.getTransaction().begin();

        // tree:
        // org1 owned by jdoe
        // -- org1A
        // -- org1B
        // resource is owned by org1
        // org1A is SuperUser on resource
        // org1B is Administrator on resource

        HawkularUser jdoe = new HawkularUser(UUID.randomUUID().toString());
        Organization org1 = new Organization(jdoe);
        Organization org1A = new Organization(org1);
        Organization org1B = new Organization(org1);
        Resource resource = new Resource(org1);

        Role superUser = new Role("SuperUser", "can do anything");
        Role administrator = new Role("Administrator", "not quite everything");

        OrganizationMembership jdoeOnOrg1SuperUser = new OrganizationMembership(org1, jdoe, superUser);
        OrganizationMembership jdoeOnOrg1Admin = new OrganizationMembership(org1, jdoe, administrator);
        OrganizationMembership org1OnOrg1ASuperUser = new OrganizationMembership(org1A, org1, superUser);
        OrganizationMembership org1OnOrg1AAdmin = new OrganizationMembership(org1A, org1, administrator);
        OrganizationMembership org1OnOrg1BSuperUser = new OrganizationMembership(org1B, org1, superUser);
        OrganizationMembership org1OnOrg1BAdmin = new OrganizationMembership(org1B, org1, administrator);

        PersonaResourceRole personaResourceRole = new PersonaResourceRole(org1A, superUser, resource);
        PersonaResourceRole personaResourceRole2 = new PersonaResourceRole(org1B, administrator, resource);

        entityManager.persist(jdoe);
        entityManager.persist(org1);
        entityManager.persist(org1A);
        entityManager.persist(org1B);
        entityManager.persist(jdoeOnOrg1SuperUser);
        entityManager.persist(jdoeOnOrg1Admin);
        entityManager.persist(org1OnOrg1ASuperUser);
        entityManager.persist(org1OnOrg1AAdmin);
        entityManager.persist(org1OnOrg1BSuperUser);
        entityManager.persist(org1OnOrg1BAdmin);
        entityManager.persist(resource);
        entityManager.persist(superUser);
        entityManager.persist(administrator);
        entityManager.persist(personaResourceRole);
        entityManager.persist(personaResourceRole2);
        entityManager.getTransaction().commit();

        Set<Role> rolesForResource = personaService.getEffectiveRolesForResource(jdoe, resource);

        assertEquals("User should have Administrator role on resource", 2, rolesForResource.size());
        boolean adminFound = false;
        boolean superUserFound = false;
        for (Role role : rolesForResource) {
            if (role.getName().equals("SuperUser")) {
                superUserFound = true;
            }
            if (role.getName().equals("Administrator")) {
                adminFound = true;
            }
        }

        assertTrue("User should have had super persona roles", superUserFound);
        assertTrue("User should have had admin roles", adminFound);
    }

}
