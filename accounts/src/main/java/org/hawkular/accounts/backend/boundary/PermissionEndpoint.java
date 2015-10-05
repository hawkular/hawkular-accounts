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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.hawkular.accounts.api.OperationService;
import org.hawkular.accounts.api.PermissionChecker;
import org.hawkular.accounts.api.ResourceService;
import org.hawkular.accounts.api.model.Operation;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.Resource;
import org.hawkular.accounts.backend.entity.rest.PermissionResponse;

/**
 * @author Juraci Paixão Kröhling
 */
@Path("/permissions")
@PermitAll
@Stateless
public class PermissionEndpoint {

    @Inject
    PermissionChecker permissionChecker;

    @Inject
    OperationService operationService;

    @Inject
    ResourceService resourceService;

    @Inject
    Persona persona;

    @GET
    public Response isAllowedTo(@QueryParam("operation") String operationName,
                                @QueryParam("resourceId") String resourceId) {

        if (null == resourceId) {
            String message = "The given resource ID is invalid (null).";
            return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
        }

        if (null == operationName || operationName.isEmpty()) {
            String message = "The given operation name is invalid (null or empty).";
            return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
        }

        Resource resource = resourceService.get(resourceId);
        if (null == resource) {
            String message = "The given resource ID is invalid (not found).";
            return Response.status(Response.Status.NOT_FOUND).entity(message).build();
        }

        Operation operation = operationService.getByName(operationName);
        if (null == operation) {
            String message = "The given operation is invalid (not found).";
            return Response.status(Response.Status.NOT_FOUND).entity(message).build();
        }

        boolean isAllowedTo = permissionChecker.isAllowedTo(operation, resource);
        PermissionResponse response = new PermissionResponse(isAllowedTo);

        return Response.ok(response).build();
    }

}
