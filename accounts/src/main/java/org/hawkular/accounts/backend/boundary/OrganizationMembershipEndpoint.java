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

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.hawkular.accounts.api.NamedOperation;
import org.hawkular.accounts.api.OrganizationMembershipService;
import org.hawkular.accounts.api.OrganizationService;
import org.hawkular.accounts.api.PermissionChecker;
import org.hawkular.accounts.api.RoleService;
import org.hawkular.accounts.api.internal.adapter.HawkularAccounts;
import org.hawkular.accounts.api.model.Operation;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationMembership;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.Role;
import org.hawkular.accounts.backend.entity.rest.ErrorResponse;
import org.hawkular.accounts.backend.entity.rest.OrganizationMembershipUpdateRequest;

/**
 * REST service responsible for managing {@link org.hawkular.accounts.api.model.OrganizationMembership}.
 *
 * @author Juraci Paixão Kröhling
 */
@Path("/organizationMemberships")
@PermitAll
@Stateless
public class OrganizationMembershipEndpoint {
    @Inject
    @HawkularAccounts
    EntityManager em;

    @Inject
    OrganizationMembershipService membershipService;

    @Inject
    OrganizationService organizationService;

    @Inject
    RoleService roleService;

    @Inject
    Persona persona;

    @Inject
    PermissionChecker permissionChecker;

    @Inject
    @NamedOperation("organization-change-role-of-members")
    Operation changeMemberRole;

    @GET
    public Response getOrganizationMembershipsForOrganization(@QueryParam("organizationId") String organizationId) {
        Organization organization = organizationService.get(organizationId);
        List<OrganizationMembership> memberships = membershipService.getMembershipsForOrganization(organization);
        return Response.ok().entity(memberships).build();
    }

    @PUT
    @Path("{membershipId}")
    public Response updateMembership(@PathParam("membershipId") String membershipId,
                                     @NotNull OrganizationMembershipUpdateRequest request) {
        if (null == membershipId || membershipId.isEmpty()) {
            String message = "The given membership ID is invalid (null).";
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(message)).build();
        }

        if (null == request.getRole() || null == request.getRole().getName()) {
            String message = "The given role is invalid (null).";
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(message)).build();
        }

        OrganizationMembership membership = membershipService.getMembershipById(membershipId);
        Role role = roleService.getByName(request.getRole().getName());

        if (null == membership) {
            String message = "The specified membership is invalid (not found).";
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse(message)).build();
        }

        if (null == role) {
            String message = "The specified role is invalid (not found).";
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse(message)).build();
        }

        if (!permissionChecker.isAllowedTo(changeMemberRole, membership.getOrganization().getId())) {
            String message = "Insufficient permissions to change the role of users of this organization.";
            return Response.status(Response.Status.FORBIDDEN).entity(new ErrorResponse(message)).build();
        }

        membership.setRole(role);
        em.persist(membership);

        return Response.ok(membership).build();
     }
}
