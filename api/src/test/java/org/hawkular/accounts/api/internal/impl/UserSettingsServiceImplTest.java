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

import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.UserSettings;
import org.junit.Test;

/**
 * @author Juraci Paixão Kröhling
 */
public class UserSettingsServiceImplTest extends SessionEnabledTest {
    @Test
    public void createSettingsIfNoneExists() {
        HawkularUser user = userService.getOrCreateById(UUID.randomUUID().toString());
        UserSettings settings = settingsService.getOrCreateByUser(user);

        assertTrue("The settings should be empty at this point.", settings.isEmpty());
    }

    @Test
    public void doNotCreateSettingsIfNoneExists() {
        HawkularUser user = userService.getOrCreateById(UUID.randomUUID().toString());
        UserSettings settings = settingsService.getByUser(user);
        assertNull("The settings should be empty at this point.", settings);
    }

    @Test
    public void storeNonExistingKey() {
        HawkularUser user = userService.getOrCreateById(UUID.randomUUID().toString());

        settingsService.store(user, "hawkular.settings.foo", "bar");

        UserSettings settings = settingsService.getByUser(user);
        assertEquals("One key should be stored at this point.", 1, settings.size());
        assertEquals("The key's value should be 'bar'.", "bar", settings.get("hawkular.settings.foo"));
    }

    @Test
    public void returnsDefaultValueIfNoneExists() {
        HawkularUser user = userService.getOrCreateById(UUID.randomUUID().toString());

        String value = settingsService.getSettingByKey(user, "hawkular.settings.foo", "bar");
        assertEquals("Value returned should be 'bar'.", "bar", value);
    }

    @Test
    public void removeExistingKey() {
        HawkularUser user = userService.getOrCreateById(UUID.randomUUID().toString());

        settingsService.store(user, "hawkular.settings.foo", "bar");

        UserSettings settings = settingsService.getByUser(user);
        assertEquals("One key should be stored at this point.", 1, settings.size());
        assertEquals("The key's value should be 'bar'.", "bar", settings.get("hawkular.settings.foo"));

        settingsService.remove(user, "hawkular.settings.foo");
        settings = settingsService.getByUser(user);
        String value = settingsService.getSettingByKey(user, "hawkular.settings.foo");
        assertEquals("There should be no settings at this point.", 0, settings.size());
        assertEquals("Requesting the previously existing setting should return null.", null, value);
    }

    @Test
    public void removeNonExistingKeyFromExistingSettings() {
        HawkularUser user = userService.getOrCreateById(UUID.randomUUID().toString());

        settingsService.store(user, "hawkular.settings.foo", "bar");

        UserSettings settings = settingsService.getByUser(user);
        assertEquals("One key should be stored at this point.", 1, settings.size());
        assertEquals("The key's value should be 'bar'.", "bar", settings.get("hawkular.settings.foo"));

        settingsService.remove(user, "NON_EXISTING_KEY");
        String value = settingsService.getSettingByKey(user, "NON_EXISTING_KEY");
        assertEquals("Requesting the a non-existing setting should return null.", null, value);
    }

    @Test
    public void removeNonExistingKeyFromNonExistingSettings() {
        HawkularUser user = userService.getOrCreateById(UUID.randomUUID().toString());

        settingsService.remove(user, "NON_EXISTING_KEY");
        String value = settingsService.getSettingByKey(user, "NON_EXISTING_KEY");
        assertEquals("Requesting the a non-existing setting should return null.", null, value);
    }

    @Test
    public void overwriteExistingKey() {
        HawkularUser user = userService.getOrCreateById(UUID.randomUUID().toString());

        settingsService.store(user, "hawkular.settings.foo", "bar");

        UserSettings settings = settingsService.getByUser(user);
        assertEquals("One key should be stored at this point.", 1, settings.size());
        assertEquals("The key's value should be 'bar'.", "bar", settings.get("hawkular.settings.foo"));

        settingsService.store(user, "hawkular.settings.foo", "baz");

        settings = settingsService.getByUser(user);
        assertEquals("One key should be stored at this point.", 1, settings.size());
        assertEquals("The key's value should be 'baz'.", "baz", settings.get("hawkular.settings.foo"));
    }

}
