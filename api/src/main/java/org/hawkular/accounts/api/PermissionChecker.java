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
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.Owner;
import org.hawkular.accounts.api.model.Resource;
import org.keycloak.KeycloakPrincipal;

/**
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
     * Determines whether the current {@link HawkularUser} has access to the resource owned by {@link Owner}
     *
     * @param currentUser the user to be checked
     * @param owner       the owner of the resource
     * @return true if the user is the owner or if the user belongs to an organization that owns the resource
     */
    boolean hasAccessTo(HawkularUser currentUser, Owner owner);

    /**
     * Determines whether the current {@link KeycloakPrincipal} has access to the resources owned by {@link Owner}.
     *
     * @param principal the {@link KeycloakPrincipal} representing the current user
     * @param owner     the owner of the resource
     * @return true if the user is the owner or if the user belongs to an organization that owns the resource
     * @see PermissionChecker#hasAccessTo(org.hawkular.accounts.api.model.HawkularUser,
     * org.hawkular.accounts.api.model.Owner)
     */
    boolean hasAccessTo(KeycloakPrincipal principal, Owner owner);

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
    boolean isMemberOf(Owner member, Organization organization);

    /**
     * Recursively checks if the specified owner is a direct or indirect owner of the given organization. For instance,
     * if jdoe is the owner of acme, and acme owns emca, then jdoe owns emca indirectly.
     *
     * @param tentativeOwner  the {@link Owner} to check. In our example above, it would be jdoe or acme. This would
     *                        usually also be the current user.
     * @param actualOwner     the actual {@link Owner} to verify ownership of. In our example above, it would be emca.
     *                        This would usually be the owner of a {@link Resource}, for instance.
     * @return whether or not the specified owner is directly or indirectly the owner of the given organization.
     */
    boolean isOwnerOf(Owner tentativeOwner, Owner actualOwner);

    /**
     * Recursively checks if the specified owner is a direct or indirect owner of the given organization. For instance,
     * if jdoe is the owner of acme, and acme owns metric1, then jdoe owns metric1 indirectly.
     *
     * @param ownerId      the {@link Owner} to check. In our example above, it would be the ID of the owner of
     *                     'metric1'
     * @return whether or not the specified owner is directly or indirectly the owner of the given organization.
     */
    boolean isOwnerOf(String ownerId);

}
