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

import java.util.List;
import java.util.UUID;

import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.PersonaResourceRole;
import org.hawkular.accounts.api.model.Resource;
import org.junit.Test;

/**
 * @author Juraci Paixão Kröhling
 */
public class ResourceServiceImplTest extends SessionEnabledTest {
    @Test
    public void nonExistingResourceIsCreatedWithOwner() {
        HawkularUser user = userService.getOrCreateById(UUID.randomUUID().toString());
        Resource resource = resourceService.create(UUID.randomUUID().toString(), user);

        assertNotNull(resourceService.get(resource.getId()));
    }

    @Test
    public void nonExistingResourceIsCreatedWithParentAndOwner() {
        HawkularUser user = userService.getOrCreateById(UUID.randomUUID().toString());
        HawkularUser user2 = userService.getOrCreateById(UUID.randomUUID().toString());
        Resource parent = resourceService.create(UUID.randomUUID().toString(), user);

        Resource resource = resourceService.create(UUID.randomUUID().toString(), parent, user2);

        Resource fromDatabase = resourceService.get(resource.getId());
        assertNotNull(fromDatabase.getPersona());
        assertNotNull(fromDatabase.getParent());
    }

    @Test
    public void nonExistingResourceIsCreatedWithParent() {
        HawkularUser user = userService.getOrCreateById(UUID.randomUUID().toString());
        Resource parent = resourceService.create(UUID.randomUUID().toString(), user);

        Resource resource = resourceService.create(UUID.randomUUID().toString(), parent);

        Resource fromDatabase = resourceService.get(resource.getId());
        assertNull(fromDatabase.getPersona());
        assertNotNull(fromDatabase.getParent());
    }

    @Test
    public void resourceWithNullAsIdGetsNewId() {
        HawkularUser user = userService.getOrCreateById(UUID.randomUUID().toString());
        Resource resource = resourceService.create(null, user);

        assertNotNull(resourceService.get(resource.getId()));
    }

    @Test
    public void revokeAll() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());
        Resource resource = resourceService.create(UUID.randomUUID().toString(), jdoe);

        resourceService.addRoleToPersona(resource, jsmith, superUser);
        resourceService.addRoleToPersona(resource, jsmith, maintainer);
        resourceService.addRoleToPersona(resource, jsmith, administrator);

        assertEquals("jsmith should have three roles", 3, resourceService.getRolesForPersona(resource, jsmith).size());

        resourceService.revokeAllForPersona(resource, jsmith);

        assertEquals("jsmith should have no roles", 0, resourceService.getRolesForPersona(resource, jsmith).size());
    }

    @Test
    public void transferResource() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());
        Resource resource = resourceService.create(UUID.randomUUID().toString(), jdoe);

        resourceService.addRoleToPersona(resource, jsmith, superUser);
        resourceService.addRoleToPersona(resource, jsmith, maintainer);
        resourceService.addRoleToPersona(resource, jsmith, administrator);

        assertEquals("jsmith should have three roles", 3, resourceService.getRolesForPersona(resource, jsmith).size());

        resourceService.transfer(resource, jsmith);

        List<PersonaResourceRole> personaResourceRoles = resourceService.getRolesForPersona(resource, jsmith);
        assertEquals("jsmith should be super user", 1, personaResourceRoles.size());
        assertEquals("jsmith should be super user", "SuperUser", personaResourceRoles.get(0).getRole().getName());
        assertEquals("jsmith should be the owner", jsmith, personaResourceRoles.get(0).getPersona());
    }

}
