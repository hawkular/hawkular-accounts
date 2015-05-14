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
package org.hawkular.accounts.api.model;

import org.hawkular.accounts.api.BaseEntityManagerEnabledTest;
import org.junit.Test;

/**
 * @author Juraci Paixão Kröhling
 */
public class RBACTest extends BaseEntityManagerEnabledTest {

    /**
     * Basic test, to demonstrate what's the expected flow of operations and how they correlate.
     */
    @Test
    public void storeUserResourceRole() {
        // basis system data
        entityManager.getTransaction().begin();
        Operation operation = new Operation("metric-create");
        Role superUser = new Role("SuperUser", "can do anything");
        Permission permission = new Permission(operation, superUser); // metric-create is allowed for SuperUser
        entityManager.persist(operation);
        entityManager.persist(superUser);
        entityManager.persist(permission);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        // user registers himself
        HawkularUser jdoe = new HawkularUser();

        // user creates a resource, he's the super user of this resource
        Resource resource = new Resource(jdoe);
        PersonaResourceRole personaResourceRole = new PersonaResourceRole(jdoe, superUser, resource);
        entityManager.persist(jdoe);
        entityManager.persist(resource);
        entityManager.persist(personaResourceRole);
        entityManager.getTransaction().commit();
    }

    @Test
    public void userCanHaveMoreThanOneRoleOnResource() {
        // basis system data
        entityManager.getTransaction().begin();
        Operation operation = new Operation("metric-create");
        Role superUser = new Role("SuperUser", "can do anything");
        Role auditor = new Role("Auditor", "can do anything");
        Permission permission = new Permission(operation, superUser); // metric-create is allowed for SuperUser
        entityManager.persist(operation);
        entityManager.persist(superUser);
        entityManager.persist(permission);
        entityManager.persist(auditor);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        // user registers himself
        HawkularUser jdoe = new HawkularUser();

        // user creates a resource, he's the super user of this resource
        Resource resource = new Resource(jdoe);
        PersonaResourceRole jdoeIsSuperUserOnResource = new PersonaResourceRole(jdoe, superUser, resource);
        PersonaResourceRole jdoeIsAuditorOnResource = new PersonaResourceRole(jdoe, superUser, resource);
        entityManager.persist(jdoe);
        entityManager.persist(resource);
        entityManager.persist(jdoeIsSuperUserOnResource);
        entityManager.persist(jdoeIsAuditorOnResource);
        entityManager.getTransaction().commit();
    }
}
