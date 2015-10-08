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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.hawkular.accounts.api.BaseEntityManagerEnabledTest;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.UserSettings;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Juraci Paixão Kröhling
 */
public class UserSettingsServiceImplTest extends BaseEntityManagerEnabledTest {

    UserSettingsServiceImpl service = new UserSettingsServiceImpl();

    @Before
    public void prepare() {
        service.em = entityManager;
    }

    @Test
    public void createSettingsIfNoneExists() {
        entityManager.getTransaction().begin();
        HawkularUser user = new HawkularUser(UUID.randomUUID().toString());
        UserSettings settings = service.getOrCreateByUser(user);
        entityManager.persist(user);
        entityManager.persist(settings);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        assertTrue("The settings should be empty at this point.", settings.isEmpty());
        entityManager.getTransaction().commit();
    }

    @Test
    public void doNotCreateSettingsIfNoneExists() {
        entityManager.getTransaction().begin();
        HawkularUser user = new HawkularUser(UUID.randomUUID().toString());
        UserSettings settings = service.getByUser(user);
        entityManager.persist(user);
        assertNull("The settings should be empty at this point.", settings);
        entityManager.getTransaction().commit();
    }

    @Test
    public void storeNonExistingKey() {
        entityManager.getTransaction().begin();
        HawkularUser user = new HawkularUser(UUID.randomUUID().toString());
        entityManager.persist(user);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        service.store(user, "hawkular.settings.foo", "bar");
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        UserSettings settings = service.getByUser(user);
        assertEquals("One key should be stored at this point.", 1, settings.size());
        assertEquals("The key's value should be 'bar'.", "bar", settings.get("hawkular.settings.foo"));
        entityManager.getTransaction().commit();
    }

    @Test
    public void returnsDefaultValueIfNoneExists() {
        entityManager.getTransaction().begin();
        HawkularUser user = new HawkularUser(UUID.randomUUID().toString());
        entityManager.persist(user);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        String value = service.getSettingByKey(user, "hawkular.settings.foo", "bar");
        assertEquals("Value returned should be 'bar'.", "bar", value);
        entityManager.getTransaction().commit();
    }

    @Test
    public void removeExistingKey() {
        entityManager.getTransaction().begin();
        HawkularUser user = new HawkularUser(UUID.randomUUID().toString());
        entityManager.persist(user);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        service.store(user, "hawkular.settings.foo", "bar");
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        UserSettings settings = service.getByUser(user);
        assertEquals("One key should be stored at this point.", 1, settings.size());
        assertEquals("The key's value should be 'bar'.", "bar", settings.get("hawkular.settings.foo"));
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        service.remove(user, "hawkular.settings.foo");
        settings = service.getByUser(user);
        String value = service.getSettingByKey(user, "hawkular.settings.foo");
        assertEquals("There should be no settings at this point.", 0, settings.size());
        assertEquals("Requesting the previously existing setting should return null.", null, value);
        entityManager.getTransaction().commit();
    }

    @Test
    public void removeNonExistingKeyFromExistingSettings() {
        entityManager.getTransaction().begin();
        HawkularUser user = new HawkularUser(UUID.randomUUID().toString());
        entityManager.persist(user);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        service.store(user, "hawkular.settings.foo", "bar");
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        UserSettings settings = service.getByUser(user);
        assertEquals("One key should be stored at this point.", 1, settings.size());
        assertEquals("The key's value should be 'bar'.", "bar", settings.get("hawkular.settings.foo"));
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        service.remove(user, "NON_EXISTING_KEY");
        String value = service.getSettingByKey(user, "NON_EXISTING_KEY");
        assertEquals("Requesting the a non-existing setting should return null.", null, value);
        entityManager.getTransaction().commit();
    }

    @Test
    public void removeNonExistingKeyFromNonExistingSettings() {
        entityManager.getTransaction().begin();
        HawkularUser user = new HawkularUser(UUID.randomUUID().toString());
        entityManager.persist(user);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        service.remove(user, "NON_EXISTING_KEY");
        String value = service.getSettingByKey(user, "NON_EXISTING_KEY");
        assertEquals("Requesting the a non-existing setting should return null.", null, value);
        entityManager.getTransaction().commit();
    }

    @Test
    public void overwriteExistingKey() {
        entityManager.getTransaction().begin();
        HawkularUser user = new HawkularUser(UUID.randomUUID().toString());
        entityManager.persist(user);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        service.store(user, "hawkular.settings.foo", "bar");
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        UserSettings settings = service.getByUser(user);
        assertEquals("One key should be stored at this point.", 1, settings.size());
        assertEquals("The key's value should be 'bar'.", "bar", settings.get("hawkular.settings.foo"));
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        service.store(user, "hawkular.settings.foo", "baz");
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        settings = service.getByUser(user);
        assertEquals("One key should be stored at this point.", 1, settings.size());
        assertEquals("The key's value should be 'baz'.", "baz", settings.get("hawkular.settings.foo"));
        entityManager.getTransaction().commit();
    }

}
