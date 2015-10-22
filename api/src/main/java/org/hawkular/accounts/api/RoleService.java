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

import java.util.Set;
import java.util.UUID;

import javax.enterprise.inject.spi.InjectionPoint;

import org.hawkular.accounts.api.model.Role;

/**
 * Provides access to information related to {@link Role}. Can be injected via CDI into managed beans as follows:
 * <p>
 *     <pre>
 *         &#64;Inject RoleService roleService;
 *     </pre>
 * </p>
 * Concrete implementations do not hold any state, but it's advised to get an instance through CDI or as an EJB.

 * @author Juraci Paixão Kröhling
 */
public interface RoleService {

    /**
     * Creates a new Role based on the given name and description.
     * @param name           the role's name. Must be unique and non-null
     * @param description    free text, can be null
     * @return the new Role
     */
    Role create(String name, String description);

    /**
     * Retrieves the persistent Role based on its ID.
     * @param id    the role ID
     * @return the role for the given ID
     */
    Role getById(UUID id);

    /**
     * Retrieves the persistent Role based on its fixed name. For instance, requesting the role with name "SuperUser"
     * would return the Role object representing the role with this name. Valid values:
     * <ul>
     *     <li>Monitor</li>
     *     <li>Operator</li>
     *     <li>Maintainer</li>
     *     <li>Deployer</li>
     *     <li>Administrator</li>
     *     <li>Auditor</li>
     *     <li>SuperUser</li>
     * </ul>
     *
     * @param name    the common name for the role.
     * @return the role for the given name
     */
    Role getByName(String name);

    /**
     * Following Wildfly's definition of roles, this method returns all roles that are implicit from the given role from
     * the User's perspective (ie: user has the given role). For instance, "SuperUser" would return all roles, while
     * "Monitor" would return an empty Set, and "Operator" would return a set containing only "Monitor".
     * <br/><br/>
     * Note that this is <b>not</b> suitable for getting which roles are allowed to perform an operation.
     *
     * @param name    the "top of hierarchy" role to get the incorporated roles from
     * @return a list of implicit roles or an empty list.
     * @see #getImplicitUserRoles(Role)
     */
    Set<Role> getImplicitUserRoles(String name);

    /**
     * Following Wildfly's definition of roles, this method returns all roles that are implicit from the given role from
     * the User's perspective (ie: user has the given role). For instance, "SuperUser" would return all roles, while
     * "Monitor" would return an empty Set, and "Operator" would return a set containing only "Monitor".
     * <br/><br/>
     * Note that this is <b>not</b> suitable for getting which roles are allowed to perform an operation.
     *
     * @param role    the "top of hierarchy" role to get the incorporated roles from
     * @return a list of implicit roles or an empty list.
     */
    Set<Role> getImplicitUserRoles(Role role);

    /**
     * Following Wildfly's definition of roles, this method returns all roles that are implicit from the given role from
     * the perspective of an operation. For instance, "SuperUser" would return an empty set meaning that only
     * "SuperUser" is allowed, while "Monitor" would return all other roles.
     * <br/><br/>
     * Note that this is <b>not</b> suitable for getting which roles an user implicitly has.
     *
     * @param name    the "top of hierarchy" role to get the incorporated roles from
     * @return a list of implicit roles or an empty list.
     */
    Set<Role> getImplicitPermittedRoles(String name);

    /**
     * Following Wildfly's definition of roles, this method returns all roles that are implicit from the given role from
     * the perspective of an operation. For instance, "SuperUser" would return an empty set meaning that only
     * "SuperUser" is allowed, while "Monitor" would return all other roles.
     * <br/><br/>
     * Note that this is <b>not</b> suitable for getting which roles an user implicitly has.
     *
     * @param role    the "top of hierarchy" role to get the incorporated roles from
     * @return a list of implicit roles or an empty list.
     */
    Set<Role> getImplicitPermittedRoles(Role role);

    /**
     * CDI producer method for Role beans annotated with
     * {@link NamedRole}. This method is not intended to be called by
     * consumers of the API.
     *
     * @param injectionPoint    the CDI InjectionPoint
     * @return the role for the name on the annotation
     */
    Role produceRoleByName(InjectionPoint injectionPoint);

    /**
     * Retrieves a Role based on the given name. If none exists, creates a new role based on the given name and
     * description.
     * @param name           the name to use in the lookup of the Role
     * @param description    a description to use, in case the role doesn't exist yet
     * @return a newly created role if there's no role with the given name, or the existing role.
     */
    Role getOrCreateByName(String name, String description);
}
