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

import org.hawkular.accounts.api.OwnerService;
import org.hawkular.accounts.api.PermissionChecker;
import org.hawkular.accounts.api.ResourceService;
import org.hawkular.accounts.api.UserService;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.Owner;
import org.hawkular.accounts.api.model.Resource;
import org.keycloak.KeycloakPrincipal;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 * @author Juraci Paixão Kröhling <juraci at kroehling.de>
 */
@Stateless
@PermitAll
public class PermissionCheckerImpl implements PermissionChecker {

    @Inject
    UserService userService;

    @Inject
    ResourceService resourceService;

    @Inject
    OwnerService ownerService;

    public boolean hasAccessTo(HawkularUser currentUser, Resource resource) {
        return hasAccessTo(currentUser, resource.getOwner());
    }

    public boolean hasAccessTo(KeycloakPrincipal principal, Resource resource) {
        return hasAccessTo(principal, resource.getOwner());
    }

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

    public boolean hasAccessTo(KeycloakPrincipal principal, Owner owner) {
        // Here's a bit of explanation: judging by the name of this class, we wouldn't expect any record to be created.
        // But in fact, the user *already* exists, just not in our database. So, on the first call of this method for
        // a new user, we create it on our side, using Keycloak's ID for this user.
        HawkularUser user = userService.getByPrincipal(principal);
        return hasAccessTo(user, owner);
    }

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
        // TODO: is this appropriate? should jdoe really have access to data from acme? if not, just remove this part
        for (Owner organizationMember : organization.getMembers()) {
            if (organizationMember instanceof Organization) {
                return isMemberOf(member, (Organization) organizationMember);
            }
        }

        return false;
    }

    public boolean isOwnerOf(Owner tentativeOwner, Owner actualOwner) {

        if (actualOwner.equals(tentativeOwner)) {
            return true;
        }

        if (actualOwner instanceof Organization) {
            Owner organizationOwner = ((Organization) actualOwner).getOwner();
            return isOwnerOf(tentativeOwner, organizationOwner);
        }

        return false;
    }

    public boolean isOwnerOf(String ownerId) {
        HawkularUser currentUser = userService.getCurrent();
        Owner owner = ownerService.getById(ownerId);
        return isOwnerOf(currentUser, owner);
    }

}
