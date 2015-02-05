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
package org.hawkular.accounts.backend.boundary;

import java.util.UUID;
import org.hawkular.accounts.backend.entity.HawkularUser;
import org.hawkular.accounts.backend.entity.Owner;
import org.hawkular.accounts.backend.entity.Resource;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Juraci Paixão Kröhling <juraci at kroehling.de>
 */
public class ResourceServiceTest extends BaseEntityManagerEnabledTest {
  private ResourceService resourceService;
  private UserService userService;

  @Before
  public void prepareServices() {
    this.resourceService = new ResourceService();
    this.userService = new UserService();
    this.userService.em = entityManager;
    this.resourceService.em = entityManager;
    this.resourceService.userService = this.userService;
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
  public void nonExistingResourceIsCreated() {
    entityManager.getTransaction().begin();
    Owner user = new HawkularUser(UUID.randomUUID().toString());
    entityManager.persist(user);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    Resource resource = resourceService.getOrCreateById(UUID.randomUUID().toString(), user);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    assertNotNull(resourceService.getById(resource.getId()));
    entityManager.getTransaction().commit();
  }
}
