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

import java.util.Map;
import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.hawkular.accounts.api.CurrentUser;
import org.hawkular.accounts.api.NamedSetting;
import org.hawkular.accounts.api.UserService;
import org.hawkular.accounts.api.UserSettingsService;
import org.hawkular.accounts.api.internal.BoundStatements;
import org.hawkular.accounts.api.internal.NamedStatement;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.UserSettings;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

/**
 * @author Juraci Paixão Kröhling
 */
@Stateless
@PermitAll
public class UserSettingsServiceImpl extends BaseServiceImpl<UserSettings> implements UserSettingsService {
    @Inject @CurrentUser
    HawkularUser user;

    @Inject
    UserService userService;

    @Inject @NamedStatement(BoundStatements.SETTINGS_GET_BY_ID)
    BoundStatement getById;

    @Inject @NamedStatement(BoundStatements.SETTINGS_GET_BY_USER)
    BoundStatement getByUser;

    @Inject @NamedStatement(BoundStatements.SETTINGS_UPDATE)
    BoundStatement updateStatement;

    @Inject @NamedStatement(BoundStatements.SETTINGS_CREATE)
    BoundStatement createStatement;

    @Override
    public UserSettings get(String id) {
        return getById(UUID.fromString(id));
    }

    @Override
    public UserSettings getById(UUID id) {
        return getById(id, getById);
    }

    @Override
    public UserSettings getByUser() {
        return getByUser(user);
    }

    @Override
    public UserSettings getByUser(HawkularUser user) {
        return getSingleRecord(getByUser.setUUID("persona", user.getIdAsUUID()));
    }

    @Override
    public UserSettings getOrCreateByUser() {
        return getOrCreateByUser(user);
    }

    @Override
    public UserSettings getOrCreateByUser(HawkularUser user) {
        UserSettings settings = getByUser(user);
        if (null == settings) {
            settings = new UserSettings(user);
            bindBasicParameters(settings, createStatement);
            createStatement.setUUID("persona", user.getIdAsUUID());
            session.execute(createStatement);
        }
        return settings;
    }

    @Override
    public String getSettingByKey(String key) {
        return getSettingByKey(user, key);
    }

    @Override
    public String getSettingByKey(String key, String defaultValue) {
        return getSettingByKey(user, key, defaultValue);
    }

    @Override
    public String getSettingByKey(HawkularUser user, String key) {
        UserSettings settings = getByUser(user);
        if (null == settings) {
            return null;
        }
        return settings.get(key);
    }

    @Override
    public String getSettingByKey(HawkularUser user, String key, String defaultValue) {
        String value = getSettingByKey(user, key);
        return value == null ? defaultValue : value;
    }

    @Override
    public UserSettings store(HawkularUser user, String key, String value) {
        UserSettings settings = getOrCreateByUser(user);
        settings.put(key, value);
        updateStatement.setMap("properties", settings.getProperties());
        update(settings, updateStatement);
        return settings;
    }

    @Override
    public UserSettings store(String key, String value) {
        return store(user, key, value);
    }

    @Override
    public UserSettings remove(HawkularUser user, String key) {
        UserSettings settings = getByUser(user);
        if (null == settings) {
            return null;
        }
        settings.remove(key);
        updateStatement.setMap("properties", settings.getProperties());
        update(settings, updateStatement);
        return settings;
    }

    @Override
    public UserSettings remove(String key) {
        return remove(user, key);
    }

    @Override
    @Produces @NamedSetting
    public String produceSettingByName(InjectionPoint injectionPoint) {
        NamedSetting namedSetting = injectionPoint.getAnnotated().getAnnotation(NamedSetting.class);
        String setting = namedSetting.value();
        return getSettingByKey(setting);
    }

    @Override
    UserSettings getFromRow(Row row) {
        HawkularUser user = userService.getById(row.getUUID("persona"));
        Map<String, String> properties = row.getMap("properties", String.class, String.class);

        UserSettings.Builder builder = new UserSettings.Builder();
        mapBaseFields(row, builder);
        return builder.user(user).properties(properties).build();
    }
}
