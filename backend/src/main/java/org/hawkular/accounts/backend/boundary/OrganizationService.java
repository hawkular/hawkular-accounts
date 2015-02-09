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
import org.hawkular.accounts.backend.entity.Organization_;
import org.hawkular.accounts.backend.entity.rest.OrganizationRequest;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * @author jpkroehling
 */
@Path("/organizations")
@PermitAll
@Stateless
public class OrganizationService {
    @Inject
    EntityManager em;

    @Resource
    SessionContext sessionContext;

    @Inject
    UserService userService;

    @Inject
    PermissionChecker permissionChecker;

    @GET
    @Path("/")
    public Response getOrganizations() {
        HawkularUser user = userService.getOrCreateById(sessionContext.getCallerPrincipal().getName());

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Organization> query = builder.createQuery(Organization.class);
        Root<Organization> root = query.from(Organization.class);
        query.select(root);
        query.where(builder.equal(root.get(Organization_.owner), user));

        return Response.ok().entity(em.createQuery(query).getResultList()).build();
    }

    @POST
    @Path("/")
    public Response createOrganization(OrganizationRequest request) {
        HawkularUser user = userService.getOrCreateById(sessionContext.getCallerPrincipal().getName());
        Organization organization = new Organization(user);

        organization.setName(request.getName());
        organization.setDescription(request.getDescription());

        em.persist(organization); // TODO: before creating, checking if it exists already
        return Response.ok().entity(organization).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteOrganization(@PathParam("id") String id) {
        Organization organization = em.find(Organization.class, id);
        HawkularUser user = userService.getById(sessionContext.getCallerPrincipal().getName());

        if (permissionChecker.isOwnerOf(user, organization)) {
            em.remove(organization);
            return Response.ok().build();
        }

        return Response.status(Response.Status.FORBIDDEN).build();
    }
}
