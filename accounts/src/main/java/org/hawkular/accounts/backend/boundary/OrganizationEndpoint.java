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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.hawkular.accounts.api.CurrentUser;
import org.hawkular.accounts.api.NamedOperation;
import org.hawkular.accounts.api.OrganizationJoinRequestService;
import org.hawkular.accounts.api.OrganizationService;
import org.hawkular.accounts.api.PermissionChecker;
import org.hawkular.accounts.api.PersonaService;
import org.hawkular.accounts.api.ResourceService;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Operation;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.Resource;
import org.hawkular.accounts.api.model.Visibility;
import org.hawkular.accounts.backend.entity.rest.ErrorResponse;
import org.hawkular.accounts.backend.entity.rest.OrganizationRequest;
import org.hawkular.accounts.backend.entity.rest.OrganizationTransferRequest;

/**
 * REST service responsible for managing {@link org.hawkular.accounts.api.model.Organization}.
 *
 * @author Juraci Paixão Kröhling
 */
@Path("/organizations")
@PermitAll
@Stateless
public class OrganizationEndpoint {
    @Inject
    Instance<Persona> personaInstance;

    @Inject @CurrentUser
    Instance<HawkularUser> userInstance;

    @Inject
    PermissionChecker permissionChecker;

    @Inject
    @NamedOperation("organization-create")
    Operation operationCreate;

    @Inject
    @NamedOperation("organization-read")
    Operation operationRead;

    @Inject
    @NamedOperation("organization-update")
    Operation operationUpdate;

    @Inject
    @NamedOperation("organization-delete")
    Operation operationDelete;

    @Inject
    @NamedOperation("organization-transfer")
    Operation operationTransfer;

    @Inject
    ResourceService resourceService;

    @Inject
    OrganizationService organizationService;

    @Inject
    PersonaService personaService;

    @Inject
    OrganizationJoinRequestService joinRequestService;

    /**
     * Retrieves all organizations to which this {@link org.hawkular.accounts.api.model.HawkularUser} has access to.
     *
     * @return a {@link javax.ws.rs.core.Response} whose entity is a {@link java.util.List} of
     * {@link org.hawkular.accounts.api.model.Organization}
     */
    @GET
    @Path("/")
    public Response getOrganizationsForPersona() {
        Persona persona = personaInstance.get();
        List<Organization> organizations = organizationService.getOrganizationsForPersona(persona);

        List<Organization> filteredOrganizations = organizations
                .stream()
                .filter(o -> permissionChecker.isAllowedTo(operationRead, o.getId(), persona))
                .collect(Collectors.toList());

        return Response.ok().entity(filteredOrganizations).build();
    }

    /**
     * Retrieves all organizations to which this {@link org.hawkular.accounts.api.model.HawkularUser} can apply to join.
     *
     * @return a {@link javax.ws.rs.core.Response} whose entity is a {@link java.util.List} of
     * {@link org.hawkular.accounts.api.model.Organization}
     */
    @GET
    @Path("/join")
    public Response getOrganizationsToJoin() {
        Persona persona = personaInstance.get();
        // we'll subtract these from the list of possible organizations to join
        List<Organization> organizationsToJoin = organizationService.getFilteredOrganizationsToJoin(persona);
        return Response.ok().entity(organizationsToJoin).build();
    }

    /**
     * Creates a new {@link org.hawkular.accounts.api.model.Organization} based on the parameters of the incoming
     * {@link org.hawkular.accounts.backend.entity.rest.OrganizationRequest}.
     *
     * <p>
     * Note that an organization cannot, currently, create an organization. The main Accounts API allows that, but we
     * block it on this endpoint as other components might not be able to deal with this situation.
     * </p>
     *
     * @param request the incoming request as {@link org.hawkular.accounts.backend.entity.rest.OrganizationRequest}
     * @return a {@link javax.ws.rs.core.Response} whose entity is an
     * {@link org.hawkular.accounts.api.model.Organization}
     */
    @POST
    @Path("/")
    public Response createOrganization(@NotNull OrganizationRequest request) {
        Persona persona = personaInstance.get();
        HawkularUser user = userInstance.get();
        if (!persona.equals(user)) {
            // HAWKULAR-180 - organizations cannot create other organizations
            // so, we check if the current persona is the same as the current user, as users can only
            // impersonate organizations, and organizations exist only when impersonated.
            String message = "Organizations cannot create sub-organizations.";
            return Response.status(Response.Status.FORBIDDEN).entity(message).build();
        }

        if (request.getName() == null || request.getName().isEmpty()) {
            String message = "Missing organization name.";
            return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
        }

        if (null != organizationService.getByName(request.getName())) {
            ErrorResponse response = new ErrorResponse("There's already an organization with this name");
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        // we *could* use Enum.valueOf here, but it might throw exceptions in two situations: null or unknown value.
        // As throwing an exception at an endpoint is a bit dangerous, we do a manual check here.
        Visibility visibility = null;
        for (Visibility value : Visibility.values()) {
            if (value.name().equalsIgnoreCase(request.getVisibility())) {
                visibility = value;
            }
        }

        if (null == visibility) {
            ErrorResponse response = new ErrorResponse(
                    "Visibility is invalid. Possible values: " + Arrays.toString(Visibility.values())
            );
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        Organization organization = organizationService.createOrganization(
                request.getName(),
                request.getDescription(),
                visibility,
                persona
        );
        return Response.ok().entity(organization).build();
    }

    /**
     * Removes an existing {@link org.hawkular.accounts.api.model.Organization} if the persona has permissions to do so.
     *
     * @param id the ID of the {@link org.hawkular.accounts.api.model.Organization} to be removed.
     * @return an empty {@link javax.ws.rs.core.Response} if successful or forbidden, if the persona has no access to
     * the organization.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteOrganization(@NotNull @PathParam("id") String id) {
        Organization organization = organizationService.get(id);
        List<Organization> subOrganizations = organizationService.getSubOrganizations(organization);

        if (subOrganizations.size() > 0) {
            ErrorResponse response = new ErrorResponse("This organization has sub-organizations. Please, remove those" +
                    " before removing this organization.");

            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        // check if there are resources
        List<Resource> resources = resourceService.getByPersona(organization);
        if (resources.size() > 0) {
            ErrorResponse response = new ErrorResponse("This organization is the owner of resources. Please, remove " +
                    "or transfer those resources before removing this organization.");

            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        // check if it's allowed to remove
        if (permissionChecker.isAllowedTo(operationDelete, id, personaInstance.get())) {
            organizationService.deleteOrganization(organization);
            return Response.ok().build();
        }

        return Response.status(Response.Status.FORBIDDEN).build();
    }

    /**
     * Retrieves a specific organization based on its ID.
     *
     * @return a {@link javax.ws.rs.core.Response} whose entity is an {@link Organization}
     */
    @GET
    @Path("/{id}")
    public Response getOrganization(@PathParam("id") String id) {
        Organization organization = organizationService.get(id);

        if (organization == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!permissionChecker.isAllowedTo(operationRead, organization.getId(), personaInstance.get())) {
            String message = "The specified organization could not be found for this persona.";
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse(message)).build();
        }

        return Response.ok().entity(organization).build();
    }

    @PUT
    @Path("/{id}")
    public Response transferOrganization(@PathParam("id") String id, OrganizationTransferRequest request) {
        if (null == id || id.isEmpty()) {
            String message = "The given organization ID is invalid (null).";
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(message)).build();
        }

        if (null == request || null == request.getOwner() || request.getOwner().getId().isEmpty()) {
            String message = "The given user ID is invalid (null).";
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(message)).build();
        }

        Organization organization = organizationService.get(id);
        Persona newOwner = personaService.get(request.getOwner().getId());

        if (null == organization) {
            String message = "The specified organization is invalid (not found).";
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse(message)).build();
        }

        if (null == newOwner) {
            String message = "The specified new owner is invalid (not found).";
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse(message)).build();
        }

        if (!permissionChecker.isAllowedTo(operationTransfer, organization.getId())) {
            String message = "Insufficient permissions to change the role of users of this organization.";
            return Response.status(Response.Status.FORBIDDEN).entity(new ErrorResponse(message)).build();
        }

        organizationService.transfer(organization, newOwner);
        return Response.ok().entity(organization).build();
    }
}
