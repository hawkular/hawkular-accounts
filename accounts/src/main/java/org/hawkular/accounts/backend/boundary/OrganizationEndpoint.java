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

import org.hawkular.accounts.api.PermissionChecker;
import org.hawkular.accounts.api.ResourceService;
import org.hawkular.accounts.api.internal.adapter.HawkularAccounts;
import org.hawkular.accounts.api.internal.adapter.NamedOperation;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Operation;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.Organization_;
import org.hawkular.accounts.backend.entity.rest.OrganizationRequest;

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

    /**
     * Retrieves all organizations to which this {@link org.hawkular.accounts.api.model.HawkularUser} has access to.
     *
     * @return a {@link javax.ws.rs.core.Response} whose entity is a {@link java.util.List} of
     * {@link org.hawkular.accounts.api.model.Organization}
     */
    @GET
    @Path("/")
    public Response getOrganizations() {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Organization> query = builder.createQuery(Organization.class);
        Root<Organization> root = query.from(Organization.class);
        query.select(root);
        query.where(builder.equal(root.get(Organization_.owner), user));

        return Response.ok().entity(em.createQuery(query).getResultList()).build();
    }

    /**
     * Creates a new {@link org.hawkular.accounts.api.model.Organization} based on the parameters of the incoming
     * {@link org.hawkular.accounts.backend.entity.rest.OrganizationRequest}.
     *
     * @param request the incoming request as {@link org.hawkular.accounts.backend.entity.rest.OrganizationRequest}
     * @return a {@link javax.ws.rs.core.Response} whose entity is an
     * {@link org.hawkular.accounts.api.model.Organization}
     */
    @POST
    @Path("/")
    public Response createOrganization(@NotNull OrganizationRequest request) {
        Organization organization = new Organization(user);
        resourceService.create(organization.getId(), user);

        organization.setName(request.getName());
        organization.setDescription(request.getDescription());

        em.persist(organization); // TODO: before creating, checking if it exists already
        return Response.ok().entity(organization).build();
    }

    /**
     * Removes an existing {@link org.hawkular.accounts.api.model.Organization} if the user has permissions to do so.
     *
     * @param id the ID of the {@link org.hawkular.accounts.api.model.Organization} to be removed.
     * @return an empty {@link javax.ws.rs.core.Response} if successful or forbidden, if the user has no access to
     * the organization.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteOrganization(@NotNull @PathParam("id") String id) {
        Organization organization = em.find(Organization.class, id);
        if (permissionChecker.isAllowedTo(operationDelete, id, user)) {
            em.remove(organization);
            return Response.ok().build();
        }

        return Response.status(Response.Status.FORBIDDEN).build();
    }
}
