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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.hawkular.accounts.api.BaseEntityManagerEnabledTest;
import org.junit.Test;

/**
 * @author Juraci Paixão Kröhling
 */
public class ResourceTest extends BaseEntityManagerEnabledTest {

    @Test
    public void testResourceWithoutParent() {
        Persona persona = new HawkularUser();
        Resource resource = new Resource(persona);
        entityManager.getTransaction().begin();
        entityManager.persist(persona);
        entityManager.persist(resource);
        entityManager.getTransaction().commit();
    }

    @Test
    public void testChangeOfParents() {
        Persona persona = new HawkularUser();
        Resource host1 = new Resource(persona);
        Resource host2 = new Resource(persona);
        Resource memoryHost1 = new Resource(host1);

        entityManager.getTransaction().begin();
        entityManager.persist(persona);
        entityManager.persist(host1);
        entityManager.persist(host2);
        entityManager.persist(memoryHost1);
        entityManager.getTransaction().commit();

        assertEquals("There should be 1 sub resource for host1", 1, host1.getSubResources().size());
        assertEquals("There should be no sub resources for host2", 0, host2.getSubResources().size());

        memoryHost1.setParent(host2);

        entityManager.getTransaction().begin();
        entityManager.persist(persona);
        entityManager.persist(host1);
        entityManager.persist(host2);
        entityManager.persist(memoryHost1);
        entityManager.getTransaction().commit();

        assertEquals("There should be no sub resources for host1", 0, host1.getSubResources().size());
        assertEquals("There should be 1 sub resource for host2", 1, host2.getSubResources().size());
    }

    @Test
    public void testResetOfParents() {
        Persona persona = new HawkularUser();
        Resource host1 = new Resource(persona);
        Resource host2 = new Resource(persona);
        Resource memoryHost1 = new Resource(host1);

        entityManager.getTransaction().begin();
        entityManager.persist(persona);
        entityManager.persist(host1);
        entityManager.persist(host2);
        entityManager.persist(memoryHost1);
        entityManager.getTransaction().commit();

        assertEquals("There should be 1 sub resource for host1", 1, host1.getSubResources().size());
        assertEquals("There should be no sub resources for host2", 0, host2.getSubResources().size());

        memoryHost1.setPersona(persona);
        memoryHost1.setParent(null);

        entityManager.getTransaction().begin();
        entityManager.persist(persona);
        entityManager.persist(host1);
        entityManager.persist(host2);
        entityManager.persist(memoryHost1);
        entityManager.getTransaction().commit();

        assertEquals("There should be no sub resources for host1", 0, host1.getSubResources().size());
        assertEquals("There should be no sub resources for host2", 0, host2.getSubResources().size());
        assertNull("There should be no sub resources for host2", memoryHost1.getParent());
    }

    @Test(expected = IllegalStateException.class)
    public void testResetWithNullOwner() {
        Persona persona = new HawkularUser();
        Resource host1 = new Resource(persona);
        Resource host2 = new Resource(persona);
        Resource memoryHost1 = new Resource(host1);

        entityManager.getTransaction().begin();
        entityManager.persist(persona);
        entityManager.persist(host1);
        entityManager.persist(host2);
        entityManager.persist(memoryHost1);
        entityManager.getTransaction().commit();

        assertEquals("There should be 1 sub resource for host1", 1, host1.getSubResources().size());
        assertEquals("There should be no sub resources for host2", 0, host2.getSubResources().size());

        memoryHost1.setParent(null);
    }

    @Test(expected = IllegalStateException.class)
    public void testParentAndOwnerNullOnConstructor() {
        Resource resource = null;
        Persona persona = null;
        new Resource(persona, resource);
    }

    @Test(expected = IllegalStateException.class)
    public void testParentNullOnConstructor() {
        Resource resource = null;
        new Resource(resource);
    }

    @Test(expected = IllegalStateException.class)
    public void testOwnerNullOnConstructor() {
        Persona persona = null;
        new Resource(persona);
    }

    @Test(expected = IllegalStateException.class)
    public void testResourceWithIdAndNullOwnerOnConstructor() {
        Persona persona = null;
        new Resource("id", persona);
    }

    @Test(expected = IllegalStateException.class)
    public void testResourceWithIdAndNullOwnerNullParentOnConstructor() {
        Persona persona = null;
        Resource resource = null;
        new Resource("id", persona, resource);
    }
}
