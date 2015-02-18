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

import org.hawkular.accounts.api.model.HawkularUser;
import org.keycloak.KeycloakPrincipal;

/**
 * @author Juraci Paixão Kröhling <juraci at kroehling.de>
 */
public interface UserService {
    /**
     * Retrieves the current user for the request.
     *
     * @return the current user
     */
    HawkularUser getCurrent();

    /**
     * Retrieves an {@link HawkularUser} based in its ID.
     *
     * @param id the user ID
     * @return the existing user with the given ID or null if the user is not found.
     */
    HawkularUser getById(String id);

    /**
     * Retrieves an {@link HawkularUser} based on the {@link KeycloakPrincipal}.
     *
     * @param principal the {@link KeycloakPrincipal}
     * @return an {@link HawkularUser} instance representing the user for the {@link KeycloakPrincipal}.It's never null.
     */
    HawkularUser getByPrincipal(KeycloakPrincipal principal);

    /**
     * Retrieves an {@link HawkularUser} based on its ID. If no user is found, a new one is created and returned.
     *
     * @param id the user ID
     * @return an {@link HawkularUser} instance representing the user with the given ID. It's never null.
     */
    HawkularUser getOrCreateById(String id);
}
