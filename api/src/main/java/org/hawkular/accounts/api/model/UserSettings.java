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

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Juraci Paixão Kröhling
 */
public class UserSettings extends BaseEntity {
    private HawkularUser user;

    private Map<String, String> properties = new HashMap<>();

    public UserSettings(HawkularUser user) {
        this.user = user;
    }

    public UserSettings(HawkularUser user, Map<String, String> properties) {
        this.user = user;
        this.properties.putAll(properties);
    }

    public HawkularUser getUser() {
        return user;
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(this.properties);
    }

    public UserSettings(UUID id, ZonedDateTime createdAt, ZonedDateTime updatedAt,
                        HawkularUser user, Map<String, String> properties) {
        super(id, createdAt, updatedAt);
        this.user = user;
        this.properties = properties;
    }

    public static class Builder extends BaseEntity.Builder {
        private HawkularUser user;
        private Map<String, String> properties;

        public Builder user(HawkularUser user) {
            this.user = user;
            return this;
        }

        public Builder properties(Map<String, String> properties) {
            this.properties = new HashMap<>(properties);
            return this;
        }

        public UserSettings build() {
            return new UserSettings(id, createdAt, updatedAt, user, properties);
        }
    }

    // delegate methods for making it easier to treat this as if it's a map
    public boolean containsKey(String o) {
        return properties.containsKey(o);
    }

    public boolean containsValue(String o) {
        return properties.containsValue(o);
    }

    public String get(String o) {
        return properties.get(o);
    }

    public String put(String s, String s2) {
        return properties.put(s, s2);
    }

    public String remove(String o) {
        return properties.remove(o);
    }

    public void putAll(Map<? extends String, ? extends String> map) {
        properties.putAll(map);
    }

    public void clear() {
        properties.clear();
    }

    public Set<String> keySet() {
        return properties.keySet();
    }

    public Collection<String> values() {
        return properties.values();
    }

    public Set<Map.Entry<String, String>> entrySet() {
        return properties.entrySet();
    }

    public void replaceAll(
            BiFunction<? super String, ? super String, ? extends String> function) {
        properties.replaceAll(function);
    }

    public void forEach(BiConsumer<? super String, ? super String> action) {
        properties.forEach(action);
    }

    public String getOrDefault(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    public String putIfAbsent(String key, String value) {
        return properties.putIfAbsent(key, value);
    }

    public boolean remove(String key, String value) {
        return properties.remove(key, value);
    }

    public boolean replace(String key, String oldValue, String newValue) {
        return properties.replace(key, oldValue, newValue);
    }

    public String replace(String key, String value) {
        return properties.replace(key, value);
    }

    public String computeIfAbsent(String key,
                                  Function<? super String, ? extends String> mappingFunction) {
        return properties.computeIfAbsent(key, mappingFunction);
    }

    public String computeIfPresent(String key,
                                   BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
        return properties.computeIfPresent(key, remappingFunction);
    }

    public String compute(String key,
                          BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
        return properties.compute(key, remappingFunction);
    }

    public String merge(String key, String value,
                        BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
        return properties.merge(key, value, remappingFunction);
    }

    public int size() {
        return properties.size();
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }
}
