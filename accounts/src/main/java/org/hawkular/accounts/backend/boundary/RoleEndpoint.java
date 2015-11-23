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

import java.util.Set;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.hawkular.accounts.api.PersonaService;
import org.hawkular.accounts.api.ResourceService;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.Resource;
import org.hawkular.accounts.api.model.Role;

/**
 * @author Juraci Paixão Kröhling
 */
@Path("/roles")
@PermitAll
@Stateless
public class RoleEndpoint {

    @Inject
    Instance<Persona> personaInstance;

    @Inject
    PersonaService personaService;

    @Inject
    ResourceService resourceService;

    @GET
    public Response getRoleForResource(@QueryParam("resourceId") String resourceId) {
        if (null == resourceId) {
            String message = "The given resource ID is invalid (null).";
            return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
        }

        Resource resource = resourceService.get(resourceId);
        Set<Role> roles = personaService.getEffectiveRolesForResource(personaInstance.get(), resource);

        return Response.ok(roles).build();
    }

}
