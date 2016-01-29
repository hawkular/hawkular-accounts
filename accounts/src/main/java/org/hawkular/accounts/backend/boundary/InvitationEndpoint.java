/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
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
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hawkular.accounts.api.CurrentUser;
import org.hawkular.accounts.api.InvitationService;
import org.hawkular.accounts.api.NamedOperation;
import org.hawkular.accounts.api.OrganizationService;
import org.hawkular.accounts.api.PermissionChecker;
import org.hawkular.accounts.api.RoleService;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Invitation;
import org.hawkular.accounts.api.model.Operation;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.Persona;
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
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InvitationEndpoint {
    private static final MsgLogger logger = MsgLogger.LOGGER;
    private static final String DEFAULT_ROLE = "Monitor";

    @Inject
    RoleService roleService;

    @Inject
    OrganizationService organizationService;

    @Inject
    InvitationService invitationService;

    @Inject @CurrentUser
    Instance<HawkularUser> userInstance;

    @Inject
    Instance<Persona> personaInstance;

    @Inject
    Event<InvitationCreatedEvent> event;

    @Inject
    PermissionChecker permissionChecker;

    @Inject
    @NamedOperation("organization-list-invitations")
    Operation operationListInvitations;

    @Inject
    @NamedOperation("organization-invite")
    Operation operationInvite;

    @GET
    public Response listPendingInvitations(@QueryParam("organizationId") String organizationId) {
        if (null == organizationId || organizationId.isEmpty()) {
            logger.missingOrganization();
            ErrorResponse errorResponse = new ErrorResponse("Missing organization ID.");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
        }

        Organization organization = organizationService.get(organizationId);

        if (null == organization) {
            logger.organizationNotFound(organizationId);
            ErrorResponse errorResponse = new ErrorResponse("The organization could not be found.");
            return Response.status(Response.Status.NOT_FOUND).entity(errorResponse).build();
        }

        if (!permissionChecker.isAllowedTo(operationListInvitations, organizationId)) {
            logger.notAllowedToPerformOperationOnResource(
                    operationListInvitations.getName(),
                    organizationId,
                    personaInstance.get().getId()
            );
            ErrorResponse errorResponse = new ErrorResponse(
                    "Insufficient permissions to list the pending invitations for this organization."
            );
            return Response.status(Response.Status.FORBIDDEN).entity(errorResponse).build();
        }

        logger.listPendingInvitations(organizationId);
        return Response.ok(invitationService.getPendingInvitationsForOrganization(organization)).build();
    }

    @POST
    public Response inviteUserToOrganization(@NotNull InvitationRequest request) {
        if (null == request.getOrganizationId() || request.getOrganizationId().isEmpty()) {
            logger.missingOrganization();
            ErrorResponse errorResponse = new ErrorResponse("Missing organization ID.");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
        }

        HawkularUser user = userInstance.get();
        Organization organization = organizationService.get(request.getOrganizationId());

        if (null == organization) {
            logger.organizationNotFound(request.getOrganizationId());
            ErrorResponse errorResponse = new ErrorResponse("The organization could not be found.");
            return Response.status(Response.Status.NOT_FOUND).entity(errorResponse).build();
        }

        if (!permissionChecker.isAllowedTo(operationInvite, organization.getId())) {
            logger.notAllowedToPerformOperationOnResource(
                    operationInvite.getName(),
                    request.getOrganizationId(),
                    personaInstance.get().getId()
            );
            ErrorResponse errorResponse = new ErrorResponse(
                    "Insufficient permissions to invite users for this organization."
            );
            return Response.status(Response.Status.FORBIDDEN).entity(errorResponse).build();
        }

        Role role = roleService.getByName(DEFAULT_ROLE);

        if (null == request.getEmails() || request.getEmails().isEmpty()) {
            logger.missingEmails();
            ErrorResponse errorResponse = new ErrorResponse("Missing emails to send the invitation to.");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
        }

        String[] emails = request.getEmails().split("[, ]");
        for (String email : emails) {
            if (email.isEmpty()) {
                continue;
            }
            Invitation invitation = invitationService.create(email, user, organization, role);
            logger.invitationSentToDispatch(invitation.getId(), email, organization.getName());
            event.fire(new InvitationCreatedEvent(invitation));
        }

        logger.invitationsSentToDispatch();
        return Response.noContent().build();
    }

    @PUT
    public Response acceptInvitation(@NotNull InvitationAcceptRequest request) {
        HawkularUser user = userInstance.get();

        if (null == request.getToken() || request.getToken().isEmpty()) {
            logger.missingToken();
            ErrorResponse errorResponse = new ErrorResponse("The invitation couldn't be determined. Make sure " +
                    "the invitation link is correct.");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
        }

        Invitation invitation = invitationService.getByToken(request.getToken());

        if (null == invitation) {
            logger.invitationNotFound(request.getToken());
            String message = "The invitation has not been found.";
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse(message)).build();
        }

        if (user.equals(invitation.getInvitedBy())) {
            logger.invitationAcceptedBySameUser(request.getToken(), user.getId());
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

        logger.invitationAccepted(invitation.getId(), user.getId());
        invitation = invitationService.accept(invitation, user);
        return Response.ok(invitation).build();
    }
}
