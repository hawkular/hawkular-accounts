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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.hawkular.accounts.api.CurrentUser;
import org.hawkular.accounts.api.OrganizationService;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Persona;

/**
 * @author Juraci Paixão Kröhling
 */
@Path("/personas")
@PermitAll
@Stateless
public class PersonaEndpoint {
    @Inject
    Instance<Persona> personaInstance;

    @Inject @CurrentUser
    Instance<HawkularUser> userInstance;

    @Inject
    OrganizationService organizationService;

    /**
     * Retrieves all personas to which this {@link org.hawkular.accounts.api.model.HawkularUser} has access to.
     *
     * @return a {@link javax.ws.rs.core.Response} whose entity is a {@link java.util.List} of
     * {@link org.hawkular.accounts.api.model.Persona}
     */
    @GET
    @Path("/")
    public Response getPersonas() {
        HawkularUser user = userInstance.get();
        List<Persona> personas = new ArrayList<>();
        // here, we purposely get the personas for the *user*, not for the current persona
        personas.addAll(organizationService.getOrganizationsForPersona(user));
        personas.add(user);

        return Response.ok().entity(personas).build();
    }

    /**
     * Retrieves all personas to which this {@link org.hawkular.accounts.api.model.HawkularUser} has access to.
     *
     * @return a {@link javax.ws.rs.core.Response} whose entity is a {@link java.util.List} of
     * {@link org.hawkular.accounts.api.model.Persona}
     */
    @GET
    @Path("/{id:[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}}")
    public Response getPersonas(@PathParam("id") String personaId) {
        HawkularUser user = userInstance.get();
        // here, we purposely get the personas for the *user*, not for the current persona
        Optional<? extends Persona> optionalPersona = organizationService
                .getOrganizationsForPersona(user)
                .stream()
                .filter(p -> p.getIdAsUUID().equals(UUID.fromString(personaId)))
                .findFirst();

        if (optionalPersona.isPresent()) {
            return Response.ok().entity(optionalPersona.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * Retrieves all personas to which this {@link org.hawkular.accounts.api.model.HawkularUser} has access to.
     *
     * @return a {@link javax.ws.rs.core.Response} whose entity is a {@link java.util.List} of
     * {@link org.hawkular.accounts.api.model.Persona}
     */
    @GET
    @Path("/current")
    public Response getCurrentPersona() {
        return Response.ok().entity(personaInstance.get()).build();
    }

}
