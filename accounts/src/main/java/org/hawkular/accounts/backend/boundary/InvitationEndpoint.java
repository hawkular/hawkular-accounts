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

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.hawkular.accounts.api.CurrentUser;
import org.hawkular.accounts.api.InvitationService;
import org.hawkular.accounts.api.OrganizationService;
import org.hawkular.accounts.api.RoleService;
import org.hawkular.accounts.api.internal.adapter.HawkularAccounts;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Invitation;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationMembership;
import org.hawkular.accounts.api.model.Role;
import org.hawkular.accounts.backend.control.MsgLogger;
import org.hawkular.accounts.backend.entity.InvitationCreatedEvent;
import org.hawkular.accounts.backend.entity.rest.ErrorResponse;
import org.hawkular.accounts.backend.entity.rest.InvitationAcceptRequest;
import org.hawkular.accounts.backend.entity.rest.InvitationRequest;

/**
 * @author Juraci Paixão Kröhling
 */
@Path("/invitations")
@PermitAll
@Stateless
public class InvitationEndpoint {
    private static final MsgLogger logger = MsgLogger.LOGGER;
    private static final String DEFAULT_ROLE = "Monitor";

    @Inject @HawkularAccounts
    EntityManager em;

    @Inject
    RoleService roleService;

    @Inject
    OrganizationService organizationService;

    @Inject
    InvitationService invitationService;

    @Inject @CurrentUser
    HawkularUser user;

    @Inject
    Event<InvitationCreatedEvent> event;

    @POST
    public Response inviteUserToOrganization(@NotNull InvitationRequest request) {
        Organization organization = organizationService.get(request.getOrganizationId());
        Role role = roleService.getByName(DEFAULT_ROLE);

        String[] emails = request.getEmails().split("[, ]");
        for (String email : emails) {
            if (email.isEmpty()) {
                continue;
            }
            Invitation invitation = new Invitation(email, user, organization, role);
            em.persist(invitation);
            event.fire(new InvitationCreatedEvent(invitation));
        }

        return Response.noContent().build();
    }

    @PUT
    public Response acceptInvitation(@NotNull InvitationAcceptRequest request) {
        Invitation invitation = invitationService.getByToken(request.getToken());

        if (null == invitation) {
            String message = "The invitation has not been found.";
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse(message)).build();
        }

        if (user.equals(invitation.getInvitedBy())) {
            String message = "The invitation has been created by the same user who is accepting it.";
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(message)).build();
        }

        if (invitation.getAcceptedAt() != null) {
            String message = "This invitation has already been previously accepted.";

            if (!user.equals(invitation.getAcceptedBy())) {
                message = "This invitation has already been previously accepted by a different user.";
                logger.invitationReused(invitation.getId(), user.getId(), invitation.getAcceptedBy().getId());
            }

            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(message)).build();
        }

        OrganizationMembership membership = new OrganizationMembership(
                invitation.getOrganization(),
                user,
                invitation.getRole());

        invitation.setAccepted();
        invitation.setAcceptedBy(user);
        em.persist(invitation);
        em.persist(membership);

        return Response.ok(invitation).build();
    }
}
