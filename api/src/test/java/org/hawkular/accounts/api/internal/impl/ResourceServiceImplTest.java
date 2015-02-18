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
import org.hawkular.accounts.api.model.Owner;
import org.hawkular.accounts.api.model.Resource;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.KeycloakPrincipal;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Juraci Paixão Kröhling <juraci at kroehling.de>
 */
public class ResourceServiceImplTest extends BaseEntityManagerEnabledTest {
    private ResourceServiceImpl resourceService;
    private UserServiceImpl userService;

    @Before
    public void prepareServices() {
        this.resourceService = new ResourceServiceImpl();
        this.userService = new UserServiceImpl();
        this.userService.em = entityManager;
        this.resourceService.em = entityManager;
        this.resourceService.userService = this.userService;
        this.resourceService.user = new HawkularUser(UUID.randomUUID().toString());
        entityManager.getTransaction().begin();
        entityManager.persist(this.resourceService.user);
        entityManager.getTransaction().commit();
    }

    @Test
    public void existingResourceIsRetrieved() {
        entityManager.getTransaction().begin();
        Owner user = new HawkularUser(UUID.randomUUID().toString());
        String resourceId = UUID.randomUUID().toString();
        Resource resource = new Resource(resourceId, user);
        entityManager.persist(user);
        entityManager.persist(resource);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        assertNotNull(resourceService.getById(resourceId));
        entityManager.getTransaction().commit();
    }

    @Test
    public void nonExistingResourceIsCreatedWithUser() {
        entityManager.getTransaction().begin();
        Owner user = new HawkularUser(UUID.randomUUID().toString());
        entityManager.persist(user);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        Resource resource = resourceService.getOrCreate(UUID.randomUUID().toString(), user);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        assertNotNull(resourceService.getById(resource.getId()));
        entityManager.getTransaction().commit();
    }

    @Test
    public void nonExistingResourceIsCreatedWithCurrentUser() {
        entityManager.getTransaction().begin();
        Resource resource = resourceService.getOrCreate(UUID.randomUUID().toString());
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        Resource resourceFromDatabase = resourceService.getById(resource.getId());
        assertNotNull(resourceFromDatabase);
        assertEquals(resourceFromDatabase.getOwner(), resourceService.user);
        entityManager.getTransaction().commit();
    }

    @Test
    public void nonExistingResourceIsCreatedWithPrincipal() {
        entityManager.getTransaction().begin();
        String userId = UUID.randomUUID().toString();
        KeycloakPrincipal principal = new KeycloakPrincipal(userId, null);
        Owner user = new HawkularUser(userId);
        entityManager.persist(user);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        Resource resource = resourceService.getOrCreate(UUID.randomUUID().toString(), principal);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        assertNotNull(resourceService.getById(resource.getId()));
        entityManager.getTransaction().commit();
    }
}
