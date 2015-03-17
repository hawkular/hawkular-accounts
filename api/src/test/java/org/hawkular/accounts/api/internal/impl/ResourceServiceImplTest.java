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

import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
        assertNotNull(resourceService.get(resourceId));
        entityManager.getTransaction().commit();
    }

    @Test
    public void nonExistingResourceIsCreatedWithOwner() {
        entityManager.getTransaction().begin();
        Owner user = new HawkularUser(UUID.randomUUID().toString());
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
        Owner user = new HawkularUser(UUID.randomUUID().toString());
        Owner user2 = new HawkularUser(UUID.randomUUID().toString());
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
        assertNotNull(fromDatabase.getOwner());
        assertNotNull(fromDatabase.getParent());
        entityManager.getTransaction().commit();
    }

    @Test
    public void nonExistingResourceIsCreatedWithParent() {
        entityManager.getTransaction().begin();
        Owner user = new HawkularUser(UUID.randomUUID().toString());
        Resource parent = new Resource(user);
        entityManager.persist(user);
        entityManager.persist(parent);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        Resource resource = resourceService.create(UUID.randomUUID().toString(), parent);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        Resource fromDatabase = resourceService.get(resource.getId());
        assertNull(fromDatabase.getOwner());
        assertNotNull(fromDatabase.getParent());
        entityManager.getTransaction().commit();
    }

    @Test
    public void resourceWithNullAsIdGetsNewId() {
        entityManager.getTransaction().begin();
        Owner user = new HawkularUser(UUID.randomUUID().toString());
        entityManager.persist(user);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        Resource resource = resourceService.create(null, user);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        assertNotNull(resourceService.get(resource.getId()));
        entityManager.getTransaction().commit();
    }

}
