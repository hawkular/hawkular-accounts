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
package org.hawkular.accounts.api;

import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.Resource;
import org.hawkular.accounts.api.model.Role;

import java.util.Set;

/**
 * Provides an interface for querying and managing {@link Persona}. Can be injected via CDI into managed beans as
 * follows:
 * <p>
 *     <pre>
 *         &#64;Inject PersonaService personaService;
 *     </pre>
 * </p>
 * Concrete implementations do not hold any state, but it's advised to get an instance through CDI or as an EJB.
 *
 * @author Juraci Paixão Kröhling
 */
public interface PersonaService {

    /**
     * Retrieves an {@link Persona} based on a given ID.
     * @param id    the persona's ID
     * @return the persona or null if there are no persona under this ID.
     * @throws IllegalArgumentException if the ID is null
     */
    Persona get(String id);

    /**
     * Retrieves the effective role of a Persona on a given Resource. If the Persona has no direct roles on the given
     * resource, then the organizations to which this persona is checked. A combined list of roles is returned.
     * <br/><br/>
     * Example 1:<br/>
     * User "jdoe" is "SuperUser" and "Auditor" on "resource1". Both "SuperUser" and "Auditor" are returned.<br/>
     *<br/>
     * Example 2:<br/>
     * User "jdoe" is a CIO and belongs to "Operations" and to "Management" and has no direct roles on "resource1".<br/>
     * "Operations" is "Administrator" on "resource1"<br/>
     * "Management" is "SuperUser" on "resource1"<br/>
     * The effective roles for "jdoe" on "resource1" are "SuperUser" and "Administrator".<br/>
     *
     * @param persona     the persona
     * @param resource    the resource
     * @return the effective roles that the persona has on the resource or null if no permissions are set
     */
    Set<Role> getEffectiveRolesForResource(Persona persona, Resource resource);

    /**
     * Retrieves the current {@link Persona} for this request.
     * @return the current persona.
     */
    Persona getCurrent();
}
