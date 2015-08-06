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

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.hawkular.accounts.api.CurrentUser;
import org.hawkular.accounts.api.OrganizationService;
import org.hawkular.accounts.api.internal.adapter.HawkularAccounts;
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
    @HawkularAccounts
    EntityManager em;

    @Inject
    Persona persona;

    @Inject @CurrentUser
    HawkularUser user;

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
    @Path("/current")
    public Response getCurrentPersona() {
        return Response.ok().entity(persona).build();
    }

}
