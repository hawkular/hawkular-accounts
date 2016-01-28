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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hawkular.accounts.api.CurrentUser;
import org.hawkular.accounts.api.NamedOperation;
import org.hawkular.accounts.api.NamedRole;
import org.hawkular.accounts.api.OrganizationJoinRequestService;
import org.hawkular.accounts.api.OrganizationMembershipService;
import org.hawkular.accounts.api.OrganizationService;
import org.hawkular.accounts.api.PermissionChecker;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.JoinRequestStatus;
import org.hawkular.accounts.api.model.Operation;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationJoinRequest;
import org.hawkular.accounts.api.model.OrganizationMembership;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.Role;
import org.hawkular.accounts.api.model.Visibility;
import org.hawkular.accounts.backend.control.MsgLogger;
import org.hawkular.accounts.backend.entity.OrganizationJoinRequestEvent;
import org.hawkular.accounts.backend.entity.rest.ErrorResponse;
import org.hawkular.accounts.backend.entity.rest.OrganizationJoinRequestDecisionRequest;

/**
 * @author Juraci Paixão Kröhling
 */
@Path("/organizationJoinRequests")
@PermitAll
@Stateless
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrganizationJoinEndpoint {
    MsgLogger logger = MsgLogger.LOGGER;

    @Inject @CurrentUser
    Instance<HawkularUser> userInstance;

    @Inject
    Instance<Persona> personaInstance;

    @Inject
    OrganizationJoinRequestService joinRequestService;

    @Inject
    OrganizationService organizationService;

    @Inject
    Event<OrganizationJoinRequestEvent> event;

    /**
     * Represents a decision regarding the request (approve or reject)
     */
    @Inject
    @NamedOperation("organization-join-request-decision")
    Operation operationDecision;

    @Inject
    @NamedOperation("organization-read")
    Operation operationRead;

    @Inject
    PermissionChecker permissionChecker;

    @Inject
    @NamedRole("Monitor")
    Role monitor;

    @Inject
    OrganizationMembershipService membershipService;

    @Path("/{organizationId}")
    @POST
    public Response applyToJoin(@PathParam("organizationId") String organizationId) {
        if (null == organizationId || organizationId.isEmpty()) {
            ErrorResponse response = new ErrorResponse("Invalid organization (null)");
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        Organization organization = organizationService.getById(UUID.fromString(organizationId));
        if (null == organization) {
            ErrorResponse response = new ErrorResponse("Organization not found");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }

        if (membershipService.getPersonaMembershipsForOrganization(personaInstance.get(), organization).size() > 0) {
            ErrorResponse response = new ErrorResponse("You are already a member of the requested organization.");
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        if (organization.getVisibility().equals(Visibility.PRIVATE)) {
            ErrorResponse response = new ErrorResponse("This organization doesn't accept applications. You need to be" +
                    " invited in order to join this organization.");
            return Response.status(Response.Status.FORBIDDEN).entity(response).build();
        }

        OrganizationJoinRequest request = joinRequestService.create(organization, userInstance.get());
        event.fire(new OrganizationJoinRequestEvent(request));
        return Response.ok(request).build();
    }

    @Path("/{organizationId}")
    @PUT
    public Response requestDecision(OrganizationJoinRequestDecisionRequest request,
                                    @PathParam("organizationId") String organizationId) {
        if (null == organizationId || organizationId.isEmpty()) {
            ErrorResponse response = new ErrorResponse("Invalid organization (null)");
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        if (null == request.getDecision() || request.getDecision().isEmpty()) {
            ErrorResponse response = new ErrorResponse("Invalid decision (null)");
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        OrganizationJoinRequest joinRequest = joinRequestService.getById(UUID.fromString(request.getJoinRequestId()));
        if (null == joinRequest) {
            ErrorResponse response = new ErrorResponse("Join request not found");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }

        if (!joinRequest.getOrganization().getIdAsUUID().toString().equals(organizationId)) {
            ErrorResponse response = new ErrorResponse("Organization mismatch: the join request doesn't belong to " +
                    "this organization.");
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        // a decision has already been made about this request
        if (!joinRequest.getStatus().equals(JoinRequestStatus.PENDING)) {
            ErrorResponse response = null;

            // it has already been accepted, and the admin wants to accept it again
            // no harm done, just let them know we "accepted" the request, but not acted upon it
            if (request.getDecision().toUpperCase().equals("ACCEPT") &&
                    joinRequest.getStatus().equals(JoinRequestStatus.ACCEPTED)) {
                response = new ErrorResponse("The join request has already been previously accepted.");
                return Response.status(Response.Status.ACCEPTED).entity(response).build();
            }

            // it has already been rejected, and the admin wants to reject it again
            // no harm done, just let them know we "accepted" the request, but not acted upon it
            if (request.getDecision().toUpperCase().equals("REJECT") &&
                    joinRequest.getStatus().equals(JoinRequestStatus.REJECTED)) {
                response = new ErrorResponse("The join request has already been previously rejected.");
                return Response.status(Response.Status.ACCEPTED).entity(response).build();
            }

            response = new ErrorResponse("A different decision has already been made about this join request.");
            return Response.status(Response.Status.CONFLICT).entity(response).build();
        }

        // TODO: what to do if, after a request has been made, an organization has changed its visibility from APPLY
        // to PRIVATE? Should we move on, as the request was made when it was APPLY, or should we block, because the
        // current status is PRIVATE? For now, we ignore.

        String resourceId = joinRequest.getIdAsUUID().toString();
        Persona persona = personaInstance.get();
        if (!permissionChecker.isAllowedTo(operationDecision, resourceId, persona)) {
            ErrorResponse response = new ErrorResponse("Insufficient permissions to accept/reject a request on this " +
                    "organization.");
            return Response.status(Response.Status.FORBIDDEN).entity(response).build();
        }

        List<OrganizationMembership> memberships = membershipService.getPersonaMembershipsForOrganization(
                joinRequest.getPersona(),
                joinRequest.getOrganization()
        );

        if (memberships.size() > 0) {
            ErrorResponse response = new ErrorResponse("The persona who requested access is already a member of the " +
                    "organization. Marking the request as REJECTED.");
            joinRequest = joinRequestService.reject(joinRequest);
            event.fire(new OrganizationJoinRequestEvent(joinRequest));
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        switch (request.getDecision().toUpperCase()) {
            case "ACCEPT":
                joinRequest =  joinRequestService.accept(joinRequest, monitor);
                break;
            case "REJECT":
                joinRequest = joinRequestService.reject(joinRequest);
                break;
            default:
                ErrorResponse response = new ErrorResponse("Invalid decision: " + request.getDecision());
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        event.fire(new OrganizationJoinRequestEvent(joinRequest));
        return Response.ok(joinRequest).build();
    }

    @Path("/{organizationId}")
    @GET
    public Response list(@PathParam("organizationId") String organizationId, @QueryParam("filter") String filter) {
        if (null == organizationId || organizationId.isEmpty()) {
            ErrorResponse response = new ErrorResponse("Invalid organization (null)");
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        if (!permissionChecker.isAllowedTo(operationRead, organizationId, personaInstance.get())) {
            ErrorResponse response = new ErrorResponse("Insufficient permissions to see join requests for this " +
                    "organization.");
            return Response.status(Response.Status.FORBIDDEN).entity(response).build();
        }

        boolean onlyPending = true;
        if (null != filter && !filter.isEmpty()) {
            onlyPending = false;
        }

        Organization organization = organizationService.getById(UUID.fromString(organizationId));
        if (null == organization) {
            ErrorResponse response = new ErrorResponse("Organization not found");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }

        List<OrganizationJoinRequest> requests;
        if (onlyPending) {
            requests = joinRequestService.getPendingRequestsForOrganization(organization);
        } else {
            requests = joinRequestService.getAllRequestsForOrganization(organization);
        }

        return Response.ok(requests).build();
    }

    @Path("/")
    @GET
    public Response listOwnRequests() {
        List<OrganizationJoinRequest> joinRequests = joinRequestService.getAllRequestsForPersona(personaInstance.get());

        joinRequests = joinRequests
                .stream()
                .filter(j -> !j.getStatus().equals(JoinRequestStatus.ACCEPTED))
                .collect(Collectors.toList());

        return Response.ok(joinRequests).build();
    }
}
