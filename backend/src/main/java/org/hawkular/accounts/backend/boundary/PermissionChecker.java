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
package org.hawkular.accounts.backend.boundary;

import org.hawkular.accounts.backend.entity.HawkularUser;
import org.hawkular.accounts.backend.entity.Organization;
import org.hawkular.accounts.backend.entity.Owner;
import org.hawkular.accounts.backend.entity.Resource;
import org.keycloak.KeycloakPrincipal;

import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 * @author Juraci Paixão Kröhling <juraci at kroehling.de>
 */
@Stateless
public class PermissionChecker {

    @Inject
    UserService userService;

    /**
     * Determines whether the current {@link HawkularUser} has access to the {@link Resource}
     *
     * @param currentUser the user to be checked
     * @param resource    the resource to be checked
     * @return true if the user is the owner or if the user belongs to an organization that owns the resource
     */
    public boolean hasAccessTo(HawkularUser currentUser, Resource resource) {
        return hasAccessTo(currentUser, resource.getOwner());
    }

    /**
     * Determines whether the current {@KeycloakPrincipal} has access to the {@link Resource}.
     *
     * @param principal the {@link KeycloakPrincipal} representing the current user
     * @param resource  the resource to be checked
     * @return true if the user is the owner or if the user belongs to an organization that owns the resource
     * @see PermissionChecker#hasAccessTo(org.keycloak.KeycloakPrincipal, org.hawkular.accounts.backend.entity.Owner)
     */
    public boolean hasAccessTo(KeycloakPrincipal principal, Resource resource) {
        return hasAccessTo(principal, resource.getOwner());
    }

    /**
     * Determines whether the current {@link HawkularUser} has access to the resource owned by {@link Owner}
     *
     * @param currentUser the user to be checked
     * @param owner       the owner of the resource
     * @return true if the user is the owner or if the user belongs to an organization that owns the resource
     */
    public boolean hasAccessTo(HawkularUser currentUser, Owner owner) {
        if (currentUser.equals(owner)) {
            return true;
        }

        // users don't belong to users, so, if the owner is an user and is not the current user, then it doesn't
        // have access
        if (owner instanceof HawkularUser) {
            return false;
        }

        return isMemberOf(currentUser, (Organization) owner);
    }

    /**
     * Determines whether the current {@link KeycloakPrincipal} has access to the resources owned by {@link Owner}.
     *
     * @param principal the {@link KeycloakPrincipal} representing the current user
     * @param owner     the owner of the resource
     * @return true if the user is the owner or if the user belongs to an organization that owns the resource
     * @see PermissionChecker#hasAccessTo(
     *org.hawkular.accounts.backend.entity.User, org.hawkular.accounts.backend.entity.Owner)
     */
    public boolean hasAccessTo(KeycloakPrincipal principal, Owner owner) {
        // Here's a bit of explanation: judging by the name of this class, we wouldn't expect any record to be created.
        // But in fact, the user *already* exists, just not in our database. So, on the first call of this method for
        // a new user, we create it on our side, using Keycloak's ID for this user.
        HawkularUser user = userService.getByPrincipal(principal);
        return hasAccessTo(user, owner);
    }

    /**
     * Recursively checks whether an user is a member or owner of an organization. Examples:
     * <p/>
     * jdoe is owner of acme -> true
     * jdoe is member of acme -> true
     * jdoe is member of emca which is member of acme -> true
     * emca is member of acme -> true
     *
     * @param member       the member to be checked
     * @param organization the organization that might contain the user
     * @return true if the user belongs to the organization recursively
     */
    public boolean isMemberOf(Owner member, Organization organization) {
        // simplest case first: is the member a direct member of this organization?
        // example: jsmith is a member of acme
        if (organization.getMembers().contains(member)) {
            return true;
        }

        // if the member is the owner of the current organization (or any organization that owns the current
        // organization)
        // then he's directly or indirectly owner of the current organization, therefore, he's member of it
        // example: jdoe is the owner of acme, acme is owner of emca, therefore, jdoe is the owner of emca
        if (isOwnerOf(member, organization)) {
            return true;
        }

        // if the "member" is part of an organization that owns the current organization, then it's a member of it
        // example: jsmith is part of acme, acme is owner of emca, therefore, jsmith is member of emca.
        Owner organizationOwner = organization.getOwner();
        if (organizationOwner instanceof Organization) {
            if (isMemberOf(member, (Organization) organizationOwner)) {
                return true;
            }
        }

        // if the member is part of a child organization, then it's a member of this one
        // example: jdoe is member of emca, emca is member of acme, therefore, jdoe is member of acme
        for (Owner organizationMember : organization.getMembers()) {
            if (organizationMember instanceof Organization) {
                return isMemberOf(member, (Organization) organizationMember);
            }
        }

        return false;
    }

    /**
     * Recursively checks if the specified owner is a direct or indirect owner of the given organization. For instance,
     * if jdoe is the owner of acme, and acme owns emca, then jdoe owns emca indirectly.
     *
     * @param owner        the {@link Owner} to check. In our example above, it would be jdoe or acme.
     * @param organization the {@link Organization} to verify ownership of. In our example above, it would be emca.
     * @return whether or not the specified owner is directly or indirectly the owner of the given organization.
     */
    public boolean isOwnerOf(Owner owner, Organization organization) {

        if (organization.getOwner().equals(owner)) {
            return true;
        }

        Owner organizationOwner = organization.getOwner();
        if (organizationOwner instanceof Organization) {
            return isOwnerOf(owner, (Organization) organizationOwner);
        }

        return false;
    }

}
