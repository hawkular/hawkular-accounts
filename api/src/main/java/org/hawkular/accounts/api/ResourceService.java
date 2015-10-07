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

import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.PersonaResourceRole;
import org.hawkular.accounts.api.model.Resource;
import org.hawkular.accounts.api.model.Role;

/**
 * Manages {@link Resource}. A Resource can be anything that is meant to be protected by the consumer modules. For
 * instance, a Resource could be an Alert, an Inventory item or a Metric. Can be injected via CDI into managed beans
 * as follows:
 * <p>
 *     <pre>
 *         &#64;Inject ResourceService resourceService;
 *     </pre>
 * </p>
 * Concrete implementations do not hold any state, but it's advised to get an instance through CDI or as an EJB.
 *
 * @author Juraci Paixão Kröhling
 */
public interface ResourceService {
    /**
     * Retrieves a {@link Resource} based on its ID.
     *
     * @param id the resource's ID
     * @return the existing {@link Resource} or null if the resource doesn't exists.
     * @throws IllegalArgumentException if the given ID is null
     */
    Resource get(String id);

    /**
     * Creates a {@link Resource} based on its ID, owned by the specified {@link Persona}
     *
     * @param id    the ID to be assigned to this resource or null for a new UUID
     * @param persona a valid owner for this resource
     * @return the newly created {@link Resource}
     * @throws IllegalArgumentException if the persona is null
     */
    Resource create(String id, Persona persona);

    /**
     * Creates a new sub resource, based on a given ID and owning resource
     *
     * @param id    the ID to be assigned to this resource or null for a new UUID
     * @param parent a valid resource to serve as the parent of this sub resource
     * @return the newly created {@link Resource}
     * @throws IllegalArgumentException if the parent is null
     */
    Resource create(String id, Resource parent);

    /**
     * Creates a new sub resource, based on a given ID, parent and owned by the specified
     * {@link org.hawkular.accounts.api.model.Persona}
     *
     * @param id     the ID to be assigned to this resource or null for a new UUID
     * @param parent the resource's parent or null if the owner is provided
     * @param persona  the resource's owner or null if the parent is provided
     * @return the newly created {@link Resource}
     * @throws IllegalArgumentException if both the parent and the owner are null
     */
    Resource create(String id, Resource parent, Persona persona);

    /**
     * Removes a {@link Resource} based on its ID.
     *
     * @param id    the resource's ID
     * @throws IllegalArgumentException if the given ID is null
     */
    void delete(String id);

    /**
     * Lists all the resources that belong to a given persona.
     * @param persona   the persona that owns the resources
     * @return  a List of Resource containing the resources owned by the persona
     */
    List<Resource> getByPersona(Persona persona);

    /**
     * Transfers the resource to the given persona. Note that the previous owner might still have individual
     * permissions on the resource, so, consider also calling {@link #revokeAllForPersona(Resource, Persona)} if it's
     * appropriate. This method also revokes all the current permissions of the new owner, replacing all of them by a
     * single "SuperUser" role on the resource.
     *
     * @param resource    the resource to receive the new owner
     * @param persona     the persona that will be the new owner
     */
    void transfer(Resource resource, Persona persona);

    /**
     * Revoke all roles that the given persona has on the resource. Note that this does not changes the ownership of
     * a resource, so, if the persona is currently the owner of the resource, the persona will still have
     * owner-rights to it, even though no individual roles are given.
     *
     * @param resource    the resource that the person currently has roles
     * @param persona     the persona to get the access revoked
     */
    void revokeAllForPersona(Resource resource, Persona persona);

    /**
     * Adds a Role to a Persona into a Resource
     * @param resource    the resource
     * @param persona     the persona to get access to the resource
     * @param role        the role of the persona on the resource
     */
    PersonaResourceRole addRoleToPersona(Resource resource, Persona persona, Role role);

    /**
     * Retrieves the roles that a given persona has on the given resource.
     * @param resource    the resource to check
     * @param persona     the persona to check
     * @return            a list of {@link PersonaResourceRole}, with one entry per combination.
     */
    List<PersonaResourceRole> getRolesForPersona(Resource resource, Persona persona);
}
