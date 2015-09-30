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
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.hawkular.accounts.api.CurrentUser;
import org.hawkular.accounts.api.NamedOperation;
import org.hawkular.accounts.api.OrganizationService;
import org.hawkular.accounts.api.PermissionChecker;
import org.hawkular.accounts.api.ResourceService;
import org.hawkular.accounts.api.internal.adapter.HawkularAccounts;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Operation;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.Organization_;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.backend.entity.rest.ErrorResponse;
import org.hawkular.accounts.backend.entity.rest.OrganizationRequest;

/**
 * REST service responsible for managing {@link org.hawkular.accounts.api.model.Organization}.
 *
 * @author Juraci Paixão Kröhling
 */
@Path("/organizations")
@PermitAll
@Stateless
public class OrganizationEndpoint {
    @Inject @HawkularAccounts
    EntityManager em;

    @Inject
    Persona persona;

    @Inject @CurrentUser
    HawkularUser user;

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
    ResourceService resourceService;

    @Inject
    OrganizationService organizationService;

    /**
     * Retrieves all organizations to which this {@link org.hawkular.accounts.api.model.HawkularUser} has access to.
     *
     * @return a {@link javax.ws.rs.core.Response} whose entity is a {@link java.util.List} of
     * {@link org.hawkular.accounts.api.model.Organization}
     */
    @GET
    @Path("/")
    public Response getOrganizationsForPersona() {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Organization> query = builder.createQuery(Organization.class);
        Root<Organization> root = query.from(Organization.class);
        query.select(root);

        query.where(builder.equal(root.get(Organization_.owner), persona));

        return Response.ok().entity(em.createQuery(query).getResultList()).build();
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
        if (!persona.equals(user)) {
            // HAWKULAR-180 - organizations cannot create other organizations
            // so, we check if the current persona is the same as the current user, as users can only
            // impersonate organizations, and organizations exist only when impersonated.
            String message = "Organizations cannot create sub-organizations.";
            return Response.status(Response.Status.FORBIDDEN).entity(message).build();
        }

        Organization organization = organizationService.createOrganization(
                request.getName(),
                request.getDescription(),
                persona
        );
        resourceService.create(organization.getId(), persona);
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
        Organization organization = em.find(Organization.class, id);

        // check if there are sub-organizations
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Organization> query = builder.createQuery(Organization.class);
        Root<Organization> root = query.from(Organization.class);
        query.select(root);

        query.where(builder.equal(root.get(Organization_.owner), organization));
        if (em.createQuery(query).getResultList().size() > 0) {
            ErrorResponse response = new ErrorResponse("This organization has sub-organizations. Please, remove those" +
                    " before removing this organization.");

            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        // check if there are resources
        if (resourceService.getByPersona(organization).size() > 0) {
            ErrorResponse response = new ErrorResponse("This organization is the owner of resources. Please, remove " +
                    "or transfer those resources before removing this organization.");

            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        // check if it's allowed to remove
        if (permissionChecker.isAllowedTo(operationDelete, id, persona)) {
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

        return Response.ok().entity(organization).build();
    }

}
