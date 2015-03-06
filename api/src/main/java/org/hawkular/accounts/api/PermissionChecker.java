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
import org.hawkular.accounts.api.model.Member;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.Owner;
import org.hawkular.accounts.api.model.Resource;
import org.keycloak.KeycloakPrincipal;

/**
 * Central part of the API, allowing a component to perform permission checking of users against resources.
 *
 * Implementations of this interface should conform with CDI rules and be injectable into managed beans. For
 * consumers, it means that a concrete implementation of this interface can be injected via {@link javax.inject.Inject}
 *
 * @author Juraci Paixão Kröhling <juraci at kroehling.de>
 */
public interface PermissionChecker {

    /**
     * Determines whether the current {@link HawkularUser} has access to the {@link Resource}
     *
     * @param currentUser the user to be checked
     * @param resource    the resource to be checked
     * @return true if the user is the owner or if the user belongs to an organization that owns the resource
     */
    boolean hasAccessTo(HawkularUser currentUser, Resource resource);

    /**
     * Determines whether the current {@link KeycloakPrincipal} has access to the {@link Resource}.
     *
     * @param principal the {@link KeycloakPrincipal} representing the current user
     * @param resource  the resource to be checked
     * @return true if the user is the owner or if the user belongs to an organization that owns the resource
     * @see PermissionChecker#hasAccessTo(org.hawkular.accounts.api.model.HawkularUser,
     * org.hawkular.accounts.api.model.Resource)
     */
    boolean hasAccessTo(KeycloakPrincipal principal, Resource resource);

    /**
     * Recursively checks whether an user is a member or owner of an organization. Examples:
     * <p/>
     * <ul>
     *     <li>jdoe is owner of acme -> true</li>
     *     <li>jdoe is member of acme -> true</li>
     *     <li>jdoe is member of emca which is member of acme -> true</li>
     *     <li>emca is member of acme -> true</li>
     * </ul>
     *
     * @param member       the member to be checked
     * @param organization the organization that might contain the user
     * @return true if the user belongs to the organization recursively
     */
    boolean isMemberOf(Member member, Organization organization);

    /**
     * Recursively checks if the specified owner is a direct or indirect owner of the given organization. For instance,
     * if jdoe is the owner of acme, and acme owns metric1, then jdoe owns metric1 indirectly.
     *
     * @param owner        the {@link Owner} to check. In our example above, it would be the ID of the owner of
     *                     'metric1'
     * @return whether or not the specified owner is directly or indirectly the owner of the given organization.
     */
    boolean isOwnerOf(Owner owner, Organization organization);

}
