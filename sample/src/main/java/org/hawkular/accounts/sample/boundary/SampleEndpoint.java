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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.hawkular.accounts.api.NamedOperation;
import org.hawkular.accounts.api.NamedSetting;
import org.hawkular.accounts.api.PermissionChecker;
import org.hawkular.accounts.api.ResourceService;
import org.hawkular.accounts.api.UserSettingsService;
import org.hawkular.accounts.api.model.Operation;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.Resource;
import org.hawkular.accounts.sample.control.HawkularAccountsSample;
import org.hawkular.accounts.sample.entity.Sample;
import org.hawkular.accounts.sample.entity.SampleRequest;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * REST endpoint that exemplifies how to get instances of Hawkular Accounts services and how to consume them.
 *
 * @author Juraci Paixão Kröhling
 */
@Path("/samples")
@PermitAll // we bypass JAAS' protections, as we want to perform the checks inside the methods
@Stateless
public class SampleEndpoint {

    @Inject
    Instance<Persona> currentPersonaInstance;

    /**
     * A managed instance of the {@link PermissionChecker}, ready to be used.
     */
    @Inject
    PermissionChecker permissionChecker;

    /**
     * We need the {@link ResourceService} as we need to tell Hawkular Accounts about who created "what". A resource
     * is this "what".
     */
    @Inject
    ResourceService resourceService;

    /**
     * For this example, we have four operations. We get an instance of each of them injected and qualified by its name.
     */
    @Inject
    @NamedOperation("sample-create")
    Operation operationCreate;

    @Inject
    @NamedOperation("sample-read")
    Operation operationRead;

    @Inject
    @NamedOperation("sample-update")
    Operation operationUpdate;

    @Inject
    @NamedOperation("sample-delete")
    Operation operationDelete;

    @Inject
    @NamedSetting("hawkular.accounts.sample.getAllSamples")
    String allSamplesQueryPreference;

    @Inject
    UserSettingsService userSettingsService;

    @Inject @HawkularAccountsSample
    Session session;

    @GET
    public Response getAllSamples() {
        List<Row> rows = session.execute("select * from hawkular_accounts.samples").all();
        List<Sample> asSamples = rows.stream().map(this::rowToSample).collect(Collectors.toList());

        // let's save a preference as well
        userSettingsService.store("hawkular.accounts.sample.getAllSamples", "WithAllParameters");

        return Response.ok().entity(asSamples).build();
    }

    @GET
    @Path("{sampleId}")
    public Response getSample(@PathParam("sampleId") String sampleId) {
        PreparedStatement pstmt = session.prepare("select * from hawkular_accounts.samples where id = :id");
        Row row = session.execute(pstmt.bind(UUID.fromString(sampleId))).one();
        Sample sample = rowToSample(row);

        // before returning, we check if the current persona has permissions to access this.
        if (permissionChecker.isAllowedTo(operationRead, sample.getId().toString())) {
            return Response.ok().entity(sample).build();
        }

        // the current persona is not allowed, so, return a 404.
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    public Response createSample(SampleRequest request) {
        Persona currentPersona = currentPersonaInstance.get();
        PreparedStatement pstmt = session.prepare("insert into hawkular_accounts.samples" +
                "(id, name, ownerId) " +
                "values" +
                "(:id, :name, :ownerId)");

        // for this example, we allow everybody to create a sample, but there might be situations where an user can
        // only create resources if they are allowed access to some other resource.
        Sample sample = new Sample(UUID.randomUUID(), currentPersona.getIdAsUUID());
        resourceService.create(sample.getId().toString(), currentPersona);
        sample.setName(request.getName());

        BoundStatement bstmt = new BoundStatement(pstmt);
        bstmt.setUUID("id", sample.getId());
        bstmt.setString("name", sample.getName());
        bstmt.setUUID("ownerId", sample.getOwnerId());
        session.execute(bstmt);
        return Response.ok().entity(sample).build();
    }

    @DELETE
    @Path("{sampleId}")
    public Response removeSample(@PathParam("sampleId") String sampleId) {
        Resource resource = resourceService.get(sampleId);

        // check if the current user can perform this operation
        if (permissionChecker.isAllowedTo(operationDelete, resource)) {
            PreparedStatement deleteStmt = session.prepare("remove from hawkular_accounts.samples where id = :id");
            session.execute(deleteStmt.bind(UUID.fromString(sampleId)));
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    public Sample rowToSample(Row row) {
        Sample sample = new Sample(row.getUUID("id"), row.getUUID("ownerId"));
        sample.setName(row.getString("name"));
        return sample;
    }
}
