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
package org.hawkular.accounts.api;

import java.util.UUID;

import javax.enterprise.inject.spi.InjectionPoint;

import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.UserSettings;

/**
 * Provides a facility for managing user-related settings.
 *
 * @author Juraci Paixão Kröhling
 */
public interface UserSettingsService {

    /**
     * Retrieves a UserSettings object based on its ID.
     * @param id    the UserSettings ID
     * @return the UserSettings for the given ID or null if none exists.
     * @deprecated Use {@link #getById(UUID)} instead
     */
    @Deprecated
    UserSettings get(String id);

    /**
     * Retrieves a UserSettings object based on its ID.
     * @param id    the UserSettings ID
     * @return the UserSettings for the given ID or null if none exists.
     */
    UserSettings getById(UUID id);

    /**
     * Retrieves a UserSettings object for the currently logged in user or null if none exist.
     *
     * @return the existing UserSettings or null
     */
    UserSettings getByUser();

    /**
     * Retrieves a UserSettings object for the given user or null if none exist.
     *
     * @param user    the user that the settings belong to
     * @return the existing UserSettings or null
     */
    UserSettings getByUser(HawkularUser user);

    /**
     * Retrieves a UserSettings object for the currently logged in user or a new one if none exists.
     *
     * @return the existing UserSettings or null
     */
    UserSettings getOrCreateByUser();

    /**
     * Retrieves a UserSettings object for the given user or a new one if none exists.
     *
     * @param user    the user that the settings belong to
     * @return the existing UserSettings or null
     */
    UserSettings getOrCreateByUser(HawkularUser user);

    /**
     * Retrieves a setting with the given key for the currently logged in user.
     * @param key     the setting's key
     * @return  the value for the given key
     */
    String getSettingByKey(String key);

    /**
     * Retrieves a setting with the given key for the currently logged in user, returning the value specified in
     * 'default' if  no setting is available for the key.
     *
     * @param key     the setting's key
     * @param defaultValue     the value to be returned should the key not exist for the user
     * @return the value for the given key or the default value, if a value doesn't exist
     */
    String getSettingByKey(String key, String defaultValue);

    /**
     * Retrieves a setting with the given key for the specified user.
     * @param user    the user to retrieve the setting from
     * @param key     the setting's key
     * @return  the value for the given key
     */
    String getSettingByKey(HawkularUser user, String key);

    /**
     * Retrieves a setting with the given key for the specified user, returning the value specified in 'default' if
     * no setting is available for the key.
     *
     * @param user    the user to retrieve the setting from
     * @param key     the setting's key
     * @param defaultValue     the value to be returned should the key not exist for the user
     * @return the value for the given key or the default value, if a value doesn't exist
     */
    String getSettingByKey(HawkularUser user, String key, String defaultValue);

    /**
     * Stores the specified setting value under the given key, for the specified user.
     *
     * @param user    the user which the setting refers to
     * @param key     the setting's key
     * @param value   the setting's value
     */
    UserSettings store(HawkularUser user, String key, String value);

    /**
     * Stores the specified setting value under the given key, for the currently logged in user.
     *
     * @param key     the setting's key
     * @param value   the setting's value
     */
    UserSettings store(String key, String value);

    /**
     * Removes the specified setting value under the given key, for the specified user.
     *
     * @param user    the user which the setting refers to
     * @param key     the setting's key
     */
    UserSettings remove(HawkularUser user, String key);

    /**
     * Removes the specified setting value under the given key, for the currently logged in user.
     *
     * @param key     the setting's key
     */
    UserSettings remove(String key);

    /**
     * CDI producer method for beans annotated with
     * {@link NamedSetting}. This method is not intended to be called by
     * consumers of the API.
     *
     * @param injectionPoint    the CDI InjectionPoint
     * @return the value for the key on the annotation
     */
    String produceSettingByName(InjectionPoint injectionPoint);
}
