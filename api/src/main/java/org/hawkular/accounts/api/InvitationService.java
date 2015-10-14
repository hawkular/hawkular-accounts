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

import java.util.List;

import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Invitation;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationMembership;
import org.hawkular.accounts.api.model.Role;

/**
 * @author Juraci Paixão Kröhling
 */
public interface InvitationService {

    /**
     * Retrieves an {@link Invitation} based on the token.
     * @param token the token that was received by the invited user
     * @return the invitation related to the token
     */
    Invitation getByToken(String token);

    /**
     * Retrieves an {@link Invitation} based on the ID.
     * @param id    the invitation's ID
     * @return  the invitation
     */
    Invitation get(String id);

    /**
     * Retrieves the pending invitations for the given organization.
     * @param organization    the organization of which pending invitations are queried from.
     * @return  a List of Invitation which are not accepted yet.
     */
    List<Invitation> getPendingInvitationsForOrganization(Organization organization);

    /**
     * Stores an invitation with the given parameters.
     * @param email           the user that has been invited
     * @param invitedBy       the user who sent the invitation
     * @param organization    the organization for which the user was invited to
     * @param role            the role on the organization for the invited user
     * @return  the persistent Invitation
     */
    Invitation create(String email, HawkularUser invitedBy, Organization organization, Role role);

    /**
     * Marks an invitation as accepted, converting the data from {@link Invitation} into an
     * {@link OrganizationMembership}
     * @param invitation    the invitation to be accepted
     * @param user          the user that is accepting the invitation
     * @return              the updated invitation
     */
    Invitation accept(Invitation invitation, HawkularUser user);
}
