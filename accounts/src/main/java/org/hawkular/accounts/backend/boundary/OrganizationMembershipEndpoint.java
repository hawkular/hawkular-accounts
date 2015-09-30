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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.hawkular.accounts.api.OrganizationMembershipService;
import org.hawkular.accounts.api.OrganizationService;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationMembership;

/**
 * REST service responsible for managing {@link org.hawkular.accounts.api.model.OrganizationMembership}.
 *
 * @author Juraci Paixão Kröhling
 */
@Path("/organizationMemberships/{organizationId}")
@PermitAll
@Stateless
public class OrganizationMembershipEndpoint {

    @Inject
    OrganizationMembershipService membershipService;

    @Inject
    OrganizationService organizationService;

    @GET
    public Response getOrganizationMembershipsForOrganization(@PathParam("organizationId") String organizationId) {
        Organization organization = organizationService.get(organizationId);
        List<OrganizationMembership> memberships = membershipService.getMembershipsForOrganization(organization);
        return Response.ok().entity(memberships).build();
    }
}
