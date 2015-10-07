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

import org.hawkular.accounts.api.BaseEntityManagerEnabledTest;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.PersonaResourceRole;
import org.hawkular.accounts.api.model.Resource;
import org.hawkular.accounts.api.model.Role;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Juraci Paixão Kröhling
 */
public class ResourceServiceImplTest extends BaseEntityManagerEnabledTest {
    private ResourceServiceImpl resourceService;

    @Before
    public void prepareServices() {
        this.resourceService = new ResourceServiceImpl();
        this.resourceService.em = entityManager;
        this.resourceService.persona = new HawkularUser(UUID.randomUUID().toString());
        entityManager.getTransaction().begin();
        entityManager.persist(this.resourceService.persona);
        entityManager.getTransaction().commit();
    }

    @Test
    public void existingResourceIsRetrieved() {
        entityManager.getTransaction().begin();
        Persona user = new HawkularUser(UUID.randomUUID().toString());
        String resourceId = UUID.randomUUID().toString();
        Resource resource = new Resource(resourceId, user);
        entityManager.persist(user);
        entityManager.persist(resource);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        assertNotNull(resourceService.get(resourceId));
        entityManager.getTransaction().commit();
    }

    @Test
    public void nonExistingResourceIsCreatedWithOwner() {
        entityManager.getTransaction().begin();
        Persona user = new HawkularUser(UUID.randomUUID().toString());
        entityManager.persist(user);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        Resource resource = resourceService.create(UUID.randomUUID().toString(), user);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        assertNotNull(resourceService.get(resource.getId()));
        entityManager.getTransaction().commit();
    }

    @Test
    public void nonExistingResourceIsCreatedWithParentAndOwner() {
        entityManager.getTransaction().begin();
        Persona user = new HawkularUser(UUID.randomUUID().toString());
        Persona user2 = new HawkularUser(UUID.randomUUID().toString());
        Resource parent = new Resource(user);
        entityManager.persist(user);
        entityManager.persist(user2);
        entityManager.persist(parent);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        Resource resource = resourceService.create(UUID.randomUUID().toString(), parent, user2);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        Resource fromDatabase = resourceService.get(resource.getId());
        assertNotNull(fromDatabase.getPersona());
        assertNotNull(fromDatabase.getParent());
        entityManager.getTransaction().commit();
    }

    @Test
    public void nonExistingResourceIsCreatedWithParent() {
        entityManager.getTransaction().begin();
        Persona user = new HawkularUser(UUID.randomUUID().toString());
        Resource parent = new Resource(user);
        entityManager.persist(user);
        entityManager.persist(parent);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        Resource resource = resourceService.create(UUID.randomUUID().toString(), parent);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        Resource fromDatabase = resourceService.get(resource.getId());
        assertNull(fromDatabase.getPersona());
        assertNotNull(fromDatabase.getParent());
        entityManager.getTransaction().commit();
    }

    @Test
    public void resourceWithNullAsIdGetsNewId() {
        entityManager.getTransaction().begin();
        Persona user = new HawkularUser(UUID.randomUUID().toString());
        entityManager.persist(user);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        Resource resource = resourceService.create(null, user);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        assertNotNull(resourceService.get(resource.getId()));
        entityManager.getTransaction().commit();
    }

    @Test
    public void revokeAll() {
        entityManager.getTransaction().begin();
        Persona jdoe = new HawkularUser(UUID.randomUUID().toString());
        Persona jsmith = new HawkularUser(UUID.randomUUID().toString());
        Resource resource = new Resource(jdoe);
        Role superUser = new Role("SuperUser", "");
        Role admin = new Role("Administrator", "");
        Role maintainer = new Role("Maintainer", "");
        entityManager.persist(jdoe);
        entityManager.persist(jsmith);
        entityManager.persist(resource);
        entityManager.persist(superUser);
        entityManager.persist(admin);
        entityManager.persist(maintainer);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        resourceService.addRoleToPersona(resource, jsmith, superUser);
        resourceService.addRoleToPersona(resource, jsmith, maintainer);
        resourceService.addRoleToPersona(resource, jsmith, admin);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        assertEquals("jsmith should have three roles", 3, resourceService.getRolesForPersona(resource, jsmith).size());
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        resourceService.revokeAllForPersona(resource, jsmith);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        assertEquals("jsmith should have no roles", 0, resourceService.getRolesForPersona(resource, jsmith).size());
        entityManager.getTransaction().commit();
    }

    public void transferResource() {
        entityManager.getTransaction().begin();
        Persona jdoe = new HawkularUser(UUID.randomUUID().toString());
        Persona jsmith = new HawkularUser(UUID.randomUUID().toString());
        Resource resource = new Resource(jdoe);
        Role superUser = new Role("SuperUser", "");
        Role admin = new Role("Administrator", "");
        Role maintainer = new Role("Maintainer", "");
        entityManager.persist(jdoe);
        entityManager.persist(jsmith);
        entityManager.persist(resource);
        entityManager.persist(superUser);
        entityManager.persist(admin);
        entityManager.persist(maintainer);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        resourceService.addRoleToPersona(resource, jsmith, superUser);
        resourceService.addRoleToPersona(resource, jsmith, maintainer);
        resourceService.addRoleToPersona(resource, jsmith, admin);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        assertEquals("jsmith should have three roles", 3, resourceService.getRolesForPersona(resource, jsmith).size());
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        resourceService.transfer(resource, jsmith);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        List<PersonaResourceRole> personaResourceRoles = resourceService.getRolesForPersona(resource, jsmith);
        assertEquals("jsmith should be super user", 1, personaResourceRoles.size());
        assertEquals("jsmith should be super user", "SuperUser", personaResourceRoles.get(0).getRole().getName());
        assertEquals("jsmith should be the owner", jsmith, personaResourceRoles.get(0).getPersona());
        entityManager.getTransaction().commit();
    }

}
