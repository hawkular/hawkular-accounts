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

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hawkular.accounts.api.CurrentUser;
import org.hawkular.accounts.api.NamedSetting;
import org.hawkular.accounts.api.UserSettingsService;
import org.hawkular.accounts.api.internal.adapter.HawkularAccounts;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.UserSettings;
import org.hawkular.accounts.api.model.UserSettings_;

/**
 * @author Juraci Paixão Kröhling
 */
@Stateless
@PermitAll
public class UserSettingsServiceImpl implements UserSettingsService {
    @Inject @HawkularAccounts
    EntityManager em;

    @Inject @CurrentUser
    HawkularUser user;

    @Override
    public UserSettings get(String id) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<UserSettings> query = builder.createQuery(UserSettings.class);
        Root<UserSettings> root = query.from(UserSettings.class);
        query.select(root);
        query.where(builder.equal(root.get(UserSettings_.id), id));

        List<UserSettings> results = em.createQuery(query).getResultList();
        if (results.size() == 1) {
            UserSettings settings = results.get(0);
            return settings;
        }

        if (results.size() > 1) {
            throw new IllegalStateException("More than one settings object found for ID " + id);
        }

        return null;
    }

    @Override
    public UserSettings getByUser() {
        return getByUser(user);
    }

    @Override
    public UserSettings getByUser(HawkularUser user) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<UserSettings> query = builder.createQuery(UserSettings.class);
        Root<UserSettings> root = query.from(UserSettings.class);
        query.select(root);
        query.where(builder.equal(root.get(UserSettings_.user), user));

        List<UserSettings> results = em.createQuery(query).getResultList();
        if (results.size() == 1) {
            UserSettings settings = results.get(0);
            return settings;
        }

        if (results.size() > 1) {
            throw new IllegalStateException("More than one settings object found for user " + user.getId());
        }

        return null;
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
            em.persist(settings);
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
        em.persist(settings);
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
        em.persist(settings);
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
}
