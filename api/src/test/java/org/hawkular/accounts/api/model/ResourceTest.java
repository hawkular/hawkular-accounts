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

import java.util.UUID;

import org.junit.Test;

/**
 * @author Juraci Paixão Kröhling
 */
public class ResourceTest {

    @Test(expected = IllegalStateException.class)
    public void testResetWithNullOwner() {
        Persona persona = new HawkularUser(UUID.randomUUID(), "John Doe");
        Resource host1 = new Resource(persona);
        Resource host2 = new Resource(persona);
        Resource memoryHost1 = new Resource(host1);
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
        new Resource(UUID.randomUUID().toString(), persona);
    }

    @Test(expected = IllegalStateException.class)
    public void testResourceWithIdAndNullOwnerNullParentOnConstructor() {
        Persona persona = null;
        Resource resource = null;
        new Resource(UUID.randomUUID().toString(), persona, resource);
    }
}
