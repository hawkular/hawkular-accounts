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

import org.hawkular.accounts.api.model.HawkularUser;

/**
 * Provides an interface for querying and managing Hawkular users. Can be injected via CDI into managed beans as
 * follows:
 * <p>
 *     <pre>
 *         &#64;Inject UserService userService;
 *     </pre>
 * </p>
 * Concrete implementations do not hold any state, but it's advised to get an instance through CDI or as an EJB.
 *
 * @author Juraci Paixão Kröhling
 */
public interface UserService {
    /**
     * Retrieves the current user for the request. Before using this, make sure you don't mean to get the current
     * {@link org.hawkular.accounts.api.model.Persona}, which will be the correct choice most of the times.
     *
     * @return the current user
     * @see PersonaService#getCurrent()
     */
    HawkularUser getCurrent();

    /**
     * Retrieves an {@link HawkularUser} based in its ID.
     *
     * @param id the user ID
     * @return the existing user with the given ID or null if the user is not found.
     * @deprecated Use {@link #getById(UUID)} instead
     */
    @Deprecated
    HawkularUser getById(String id);

    /**
     * Retrieves an {@link HawkularUser} based in its ID.
     *
     * @param id the user ID
     * @return the existing user with the given ID or null if the user is not found.
     */
    HawkularUser getById(UUID id);

    /**
     * Retrieves an {@link HawkularUser} based on its ID. If no user is found, a new one is created and returned.
     *
     * @param id the user ID
     * @return an {@link HawkularUser} instance representing the user with the given ID. It's never null.
     */
    HawkularUser getOrCreateById(String id);

    /**
     * Retrieves an {@link HawkularUser} based on its ID. If no user is found, a new one is created and returned. The
     * name parameter is used only in case the user doesn't exist.
     *
     * @param id the user ID
     * @param name the full name for the user. Used only if the user doesn't exist, when creating the new user.
     * @return an {@link HawkularUser} instance representing the user with the given ID. It's never null.
     */
    HawkularUser getOrCreateByIdAndName(String id, String name);
}
