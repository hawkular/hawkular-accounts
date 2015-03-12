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
package org.hawkular.accounts.sample.boundary;

import org.hawkular.accounts.api.PermissionChecker;
import org.hawkular.accounts.api.ResourceService;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Resource;
import org.hawkular.accounts.sample.control.HawkularAccountsSample;
import org.hawkular.accounts.sample.entity.Sample;
import org.hawkular.accounts.sample.entity.SampleRequest;
import org.hawkular.accounts.sample.entity.Sample_;

import javax.annotation.security.PermitAll;
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
import java.util.UUID;

/**
 * @author jpkroehling
 */
@Path("/samples")
@PermitAll
@Stateless
public class SampleService {
    @Inject @HawkularAccountsSample
    EntityManager em;

    @Inject
    HawkularUser currentUser;

    @Inject
    PermissionChecker permissionChecker;

    /**
     *
     * user "A" subscribes to SaaS
     * user "B" subscribes to SaaS
     * user "C" subscribes to SaaS
     *
     * user "A" is part of the same company as "C"
     * user "A" creates an organization called "Acme, Inc"
     * user "A" adds user "C" to the "Acme, Inc"
     *
     * user "C" adds metric "CPU on machine torii.gva.....com", owned by "Acme, inc"
     * both user "A" and user "C" can see this metric
     *
     *
     * machine A (Resource): owner jdoe (Owner)
     *  - cpu (sub resource): no owner information (jdoe is the effective owner)
     *  --- cpu whatever sub metric
     *  - memory (sub resource): no owner information (jdoe is the effective owner)
     *  - application server (sub resource): owner "Operations", or someone with "operations" role
     *  - application server 2 (sub resource): owner "jsmith"
     *
     * machine A (Resource): owner Acme, Inc (Owner) (acme == jdoe, jsmith)
     *  - cpu (sub resource): no owner information (Acme is the effective owner)
     *  --- cpu whatever sub metric
     *  - memory (sub resource): owner jdoe
     *  - application server (sub resource): owner "jsmith"
     *
     * machine is owned by IT
     * - IT department has:
     *  - database server admins
     *      - see app server connection pool
     *      - not allowed to modify AS
     *  - app server admins
     *      - has read access to DB
     *      - not allowed to modify DB
     *  - app deployed on app servers
     *      - business metrics not viewable by "db admins" nor "app server admins"
     *
     *      --- Acme, Inc
     *          - Operations
     *              - DBA
     *              - Sysops
     *              - Product 1
     *                  - Sysops
     *                  - DBA
     *              - Product 2
     *                  - Sysops
     *                  - DBA
     *          - Business
     *              - Sales
     *              - Marketing
     *              - ...
     *      --- Insurance company from Munich, Inc
     *          - Operations
     *              - DBA
     *              - Sysops
     *              - Product 1
     *                  - Sysops
     *                  - DBA
     *              - Product 2
     *                  - Sysops
     *                  - DBA
     *          - Business
     *              - Sales
     *              - Marketing
     *              - ...
     *
     * common roles
     * - auditor
     * - operations
     * component-specific roles
     */

    @Inject
    ResourceService resourceService;

    @GET
    public Response getAllSamples() {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Sample> query = builder.createQuery(Sample.class);
        Root<Sample> root = query.from(Sample.class);
        query.select(root);
        query.where(builder.equal(root.get(Sample_.ownerId), currentUser.getId()));

        return Response.ok().entity(em.createQuery(query).getResultList()).build();
    }

    @GET
    @Path("{sampleId}")
    public Response getSample(@PathParam("sampleId") String sampleId) {
        Sample sample = em.find(Sample.class, sampleId);
        Resource resource = resourceService.getOrCreate(sampleId);
        if (permissionChecker.hasAccessTo(currentUser, resource)) {
            return Response.ok().entity(sample).build();
        }

        // there's an eternal discussion on whether an existing ID belonging to an user should return a
        // "forbidden", with a non-existing ID returning "not found". I personally prefer a "not found",
        // as the requested resource does not exist for *this* user.
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    public Response createSample(SampleRequest request) {
        Sample sample = new Sample(UUID.randomUUID().toString(), currentUser.getId());

        sample.setName(request.getName());

        em.persist(sample);
        return Response.ok().entity(sample).build();
    }

    @DELETE
    @Path("{sampleId}")
    public Response removeSample(@PathParam("sampleId") String sampleId) {
        Sample sample = em.find(Sample.class, sampleId);
        if (permissionChecker.isOwnerOf(resourceService.getById(sampleId))) {
            em.remove(sample);
            return Response.ok().entity(sample).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
