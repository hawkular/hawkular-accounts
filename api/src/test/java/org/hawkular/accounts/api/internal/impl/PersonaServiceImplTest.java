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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.Resource;
import org.hawkular.accounts.api.model.Role;
import org.junit.Test;

/**
 * @author Juraci Paixão Kröhling
 */
public class PersonaServiceImplTest extends BaseServicesTest {
    @Test
    public void existingIdIsFound() {
        entityManager.getTransaction().begin();
        String id = UUID.randomUUID().toString();
        entityManager.persist(userService.getOrCreateById(id));
        entityManager.getTransaction().commit();

        assertNotNull(personaService.get(id));
    }

    @Test
    public void nonExistingIdReturnsNull() {
        entityManager.getTransaction().begin();
        String id = UUID.randomUUID().toString();
        entityManager.persist(userService.getOrCreateById(id));
        entityManager.getTransaction().commit();

        assertNull(personaService.get("non-existing-id"));
    }

    @Test
    public void userHaveRolesOnResource() {
        entityManager.getTransaction().begin();
        HawkularUser user = userService.getOrCreateById(UUID.randomUUID().toString());
        Resource resource = resourceService.create(UUID.randomUUID().toString(), user);
        entityManager.getTransaction().commit();

        Set<Role> rolesForResource = personaService.getEffectiveRolesForResource(user, resource);

        assertEquals("User should have SuperUser (and all other) roles on resource", 7, rolesForResource.size());
        Role role = rolesForResource.stream().filter(Predicate.isEqual(superUser)).findFirst().get();
        assertEquals("SuperUser", role.getName());
    }

    @Test
    public void onlyRolesOnResourceShouldCount() {
        entityManager.getTransaction().begin();
        HawkularUser user = userService.getOrCreateById(UUID.randomUUID().toString());
        Resource resource = resourceService.create(UUID.randomUUID().toString(), user);
        Resource resource2 = resourceService.create(UUID.randomUUID().toString(), user);
        entityManager.getTransaction().commit();

        Set<Role> rolesForResource = personaService.getEffectiveRolesForResource(user, resource);

        assertEquals("User should have SuperUser (all other) roles on resource", 7, rolesForResource.size());
        Role role = rolesForResource.stream().filter(Predicate.isEqual(superUser)).findFirst().get();
        assertEquals("SuperUser", role.getName());
    }

    @Test
    public void directPermissionOnResourceIgnoresOrganizationRolesOnSameResource() {
        entityManager.getTransaction().begin();
        HawkularUser user = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization organization = organizationService.createOrganization("", "", user);
        Resource resource = resourceService.create(UUID.randomUUID().toString(), organization);
        resourceService.addRoleToPersona(resource, user, administrator);
        entityManager.getTransaction().commit();

        Set<Role> rolesForResource = personaService.getEffectiveRolesForResource(user, resource);

        assertEquals("User should have Administrator (and 3 other) roles on resource", 4, rolesForResource.size());
        Role role = rolesForResource.stream().filter(Predicate.isEqual(administrator)).findFirst().get();
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

        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization org1 = organizationService.createOrganization("", "", jdoe);
        Organization org1A = organizationService.createOrganization("", "", org1);
        Organization org1B = organizationService.createOrganization("", "", org1);
        Resource resource = resourceService.create(UUID.randomUUID().toString(), org1);
        entityManager.getTransaction().commit();

        Set<Role> rolesForResource = personaService.getEffectiveRolesForResource(jdoe, resource);

        assertEquals("jdoe should have all permissions on resource", 7, rolesForResource.size());
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

        assertTrue("User should have had super user roles", superUserFound);
        assertTrue("User should have had admin roles", adminFound);
    }

    @Test
    public void userCanImpersonateItself() {
        entityManager.getTransaction().begin();
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        entityManager.getTransaction().commit();

        entityManager.clear();

        entityManager.getTransaction().begin();
        assertTrue("User should be allowed to impersonate itself", personaService.isAllowedToImpersonate(jdoe, jdoe));
        entityManager.getTransaction().commit();
    }

    @Test
    public void userCanImpersonateOwnOrganization() {
        entityManager.getTransaction().begin();
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization org1 = organizationService.createOrganization("Acme, Inc", "Acme, Inc", jdoe);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        assertTrue("User should be allowed to impersonate org", personaService.isAllowedToImpersonate(jdoe, org1));
        entityManager.getTransaction().commit();
    }

    @Test
    public void userCanImpersonateSubOrganization() {
        entityManager.getTransaction().begin();
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization org1 = organizationService.createOrganization("Acme, Inc", "Acme, Inc", jdoe);
        Organization org2 = organizationService.createOrganization("IT Dep", "IT Dep", org1);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        assertTrue("User should be allowed to impersonate sub org", personaService.isAllowedToImpersonate(jdoe, org2));
        entityManager.getTransaction().commit();
    }

    @Test
    public void userCannotImpersonateRandomOrganization() {
        entityManager.getTransaction().begin();
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization org1 = organizationService.createOrganization("Another org", "Another org", jsmith);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        assertFalse("User should not be allowed to impersonate another org",
                personaService.isAllowedToImpersonate(jdoe, org1));
        entityManager.getTransaction().commit();
    }

    @Test
    public void userCannotImpersonateAnotherUser() {
        entityManager.getTransaction().begin();
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());
        entityManager.getTransaction().commit();

        entityManager.clear();

        entityManager.getTransaction().begin();
        assertFalse("User should not be allowed to impersonate another user",
                personaService.isAllowedToImpersonate(jdoe, jsmith));
        entityManager.getTransaction().commit();
    }

}
