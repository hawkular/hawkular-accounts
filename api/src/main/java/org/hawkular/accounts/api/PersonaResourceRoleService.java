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

import java.util.List;
import java.util.UUID;

import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.PersonaResourceRole;
import org.hawkular.accounts.api.model.Resource;
import org.hawkular.accounts.api.model.Role;

/**
 * @author Juraci Paixão Kröhling
 */
public interface PersonaResourceRoleService {

    /**
     * Retrieves a PersonaResourceRole relation based on the relation's ID.
     * @param id    the relation's ID
     * @return the relation
     */
    PersonaResourceRole getById(UUID id);

    /**
     * Retrieves all relations for a given persona.
     * @param persona    the persona
     * @return all roles for all resources for the given persona
     */
    List<PersonaResourceRole> getByPersona(Persona persona);

    /**
     * Retrieves all relations for the given resource.
     * @param resource    the resource
     * @return all roles for all personas for the given resource
     */
    List<PersonaResourceRole> getByResource(Resource resource);

    /**
     * Retrieves all relations for the given persona + resource
     * @param persona     the persona
     * @param resource    the resource
     * @return all roles for the combination of persona and resource
     */
    List<PersonaResourceRole> getByPersonaAndResource(Persona persona, Resource resource);

    /**
     * Creates a new relation based on the given parameters.
     * @param persona     the persona
     * @param resource    the resource
     * @param role        the role of the persona on the role
     * @return the newly created relation
     */
    PersonaResourceRole create(Persona persona, Resource resource, Role role);

    /**
     * Removes the relation with the given ID from the storage.
     * @param id    the ID of the relation
     */
    void remove(UUID id);

    /**
     * Removes the relation from the storage.
     * @param personaResourceRole    the relation to be removed
     */
    void remove(PersonaResourceRole personaResourceRole);

}
