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
public class PersonaServiceImplTest extends SessionEnabledTest {
    @Test
    public void existingIdIsFound() {
        String id = UUID.randomUUID().toString();
        userService.getOrCreateById(id);
        assertNotNull(personaService.get(id));
    }

    @Test
    public void nonExistingIdReturnsNull() {
        assertNull(personaService.get(UUID.randomUUID().toString()));
    }

    @Test
    public void userHaveRolesOnResource() {
        HawkularUser user = userService.getOrCreateById(UUID.randomUUID().toString());
        Resource resource = resourceService.create(UUID.randomUUID().toString(), user);

        Set<Role> rolesForResource = personaService.getEffectiveRolesForResource(user, resource);

        assertEquals("User should have SuperUser (and all other) roles on resource", 7, rolesForResource.size());
        Role role = rolesForResource.stream().filter(Predicate.isEqual(superUser)).findFirst().get();
        assertEquals("SuperUser", role.getName());
    }

    @Test
    public void onlyRolesOnResourceShouldCount() {
        HawkularUser user = userService.getOrCreateById(UUID.randomUUID().toString());
        Resource resource = resourceService.create(UUID.randomUUID().toString(), user);
        Resource resource2 = resourceService.create(UUID.randomUUID().toString(), user);

        Set<Role> rolesForResource = personaService.getEffectiveRolesForResource(user, resource);

        assertEquals("User should have SuperUser (all other) roles on resource", 7, rolesForResource.size());
        Role role = rolesForResource.stream().filter(Predicate.isEqual(superUser)).findFirst().get();
        assertEquals("SuperUser", role.getName());
    }

    @Test
    public void directPermissionOnResourceIgnoresOrganizationRolesOnSameResource() {
        HawkularUser user = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization organization = organizationService.createOrganization("", "", user);
        Resource resource = resourceService.create(UUID.randomUUID().toString(), organization);
        resourceService.addRoleToPersona(resource, user, administrator);

        Set<Role> rolesForResource = personaService.getEffectiveRolesForResource(user, resource);

        assertEquals("User should have Administrator (and 3 other) roles on resource", 4, rolesForResource.size());
        Role role = rolesForResource.stream().filter(Predicate.isEqual(administrator)).findFirst().get();
        assertEquals("Administrator", role.getName());
    }

    @Test
    public void organizationTreePutsUserWithTwoRolesInTwoOrganizations() {
        // tree:
        // org1 owned by jdoe
        // -- org1A
        // -- org1B
        // resource is owned by org1
        // org1A is SuperUser on resource
        // org1B is Administrator on resource

        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization org1 = organizationService.createOrganization(UUID.randomUUID().toString(), "", jdoe);
        Organization org1A = organizationService.createOrganization(UUID.randomUUID().toString(), "", org1);
        Organization org1B = organizationService.createOrganization(UUID.randomUUID().toString(), "", org1);
        Resource resource = resourceService.create(UUID.randomUUID().toString(), org1);

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
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        assertTrue("User should be allowed to impersonate itself", personaService.isAllowedToImpersonate(jdoe, jdoe));
    }

    @Test
    public void userCanImpersonateOwnOrganization() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization org1 = organizationService.createOrganization(UUID.randomUUID().toString(), "", jdoe);
        assertTrue("User should be allowed to impersonate org", personaService.isAllowedToImpersonate(jdoe, org1));
    }

    @Test
    public void userCanImpersonateSubOrganization() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization org1 = organizationService.createOrganization(UUID.randomUUID().toString(), "Acme, Inc", jdoe);
        Organization org2 = organizationService.createOrganization(UUID.randomUUID().toString(), "IT Dep", org1);
        assertTrue("User should be allowed to impersonate sub org", personaService.isAllowedToImpersonate(jdoe, org2));
    }

    @Test
    public void userCannotImpersonateRandomOrganization() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());
        Organization org1 = organizationService.createOrganization(UUID.randomUUID().toString(), "Another org", jsmith);
        assertFalse("User should not be allowed to impersonate another org",
                personaService.isAllowedToImpersonate(jdoe, org1));
    }

    @Test
    public void userCannotImpersonateAnotherUser() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());
        assertFalse("User should not be allowed to impersonate another user",
                personaService.isAllowedToImpersonate(jdoe, jsmith));
    }

}
