/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
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

import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationMembership;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.Visibility;

/**
 * Service intended to handle data related to an {@link Organization}. Can be injected via CDI into managed beans as
 * follows:
 * <p>
 *     <pre>
 *         &#64;Inject OrganizationService organizationService;
 *     </pre>
 * </p>
 * Concrete implementations do not hold any state, but it's advised to get an instance through CDI or as an EJB.
 * <p>
 *     NOTE: At this moment, it is possible to add organizations to other organizations, but this behavior is currently
 *     blocked on the main REST endpoint (see the 'accounts' module).
 * </p>
 *
 * @author Juraci Paixão Kröhling
 */
public interface OrganizationService {
    /**
     * Retrieves all organizations that a specific persona can apply to join, removing the organizations that the
     * persona already belongs or that the persona has already applied for.
     *
     * @return all non-private organizations, except for organizations the persona already belongs or already applied.
     */
    List<Organization> getFilteredOrganizationsToJoin(Persona persona);

    /**
     * Retrieves *all* organizations that are possible to get a join request. In other words: returns all non-private
     * organizations.
     *
     *
     * @return all non-private organizations
     */
    List<Organization> getOrganizationsToJoin();

    /**
     * Retrieves the Organizations to which a given Persona directly is member/owner of.
     * <br/>
     * Example:<br/>
     * User "jdoe" is member of "Operations"<br/>
     * Organization "Operations" is a member of "Acme, Inc"<br/>
     * Only "Operations" is returned by this method.<br/>
     *
     * @param persona    the persona
     * @return the organizations this persona directly belongs to or null if this persona doesn't belongs to any
     * organizations.
     */
    List<Organization> getOrganizationsForPersona(Persona persona);

    /**
     * Extracts all organizations from a list of memberships.
     *
     * @param memberships    the list of {@link OrganizationMembership}
     * @return a collection of {@link Organization} extracted from the memberships
     */
    List<Organization> getOrganizationsFromMemberships(List<OrganizationMembership> memberships);

    /**
     * Creates a new organization and a "SuperUser" membership for the persona. When using this method, the default
     * visibility PRIVATE will be used.
     *
     * @param name           the organization's name
     * @param description    the organization's description
     * @param owner          the persona that owns the organization
     * @return the newly created organization
     */
    Organization createOrganization(String name, String description, Persona owner);

    /**
     * Creates a new organization and a "SuperUser" membership for the persona.
     *
     * @param name           the organization's name
     * @param description    the organization's description
     * @param visibility     the organization's visibility.
     * @param owner          the persona that owns the organization
     * @return the newly created organization
     */
    Organization createOrganization(String name, String description, Visibility visibility, Persona owner);

    /**
     * Removes the organization and all related memberships
     * @param organization    the organization to be removed
     */
    void deleteOrganization(Organization organization);

    /**
     * Transfers the organization from the current owner to the specified persona.
     * @param organization    the organization to have the ownership changed
     * @param newOwner        the new owner
     */
    void transfer(Organization organization, Persona newOwner);

    /**
     * Retrieves an {@link Organization} based on its ID.
     *
     * @param id             the organization's ID
     * @return the existing {@link Organization} or null if the resource doesn't exists.
     * @throws IllegalArgumentException if the given ID is null
     */
    Organization getById(UUID id);

    /**
     * Retrieves an {@link Organization} based on its name.
     *
     * @param name             the organization's name
     * @return the existing {@link Organization} or null if the resource doesn't exists.
     * @throws IllegalArgumentException if the given name is null
     */
    Organization getByName(String name);

    /**
     * Retrieves an {@link Organization} based on its ID.
     *
     * @param id             the organization's ID
     * @return the existing {@link Organization} or null if the resource doesn't exists.
     * @throws IllegalArgumentException if the given ID is null
     * @deprecated use {@link #getById(UUID)} instead.
     */
    @Deprecated
    Organization get(String id);

    /**
     * Retrieves all the organizations whose parent is the given organization.
     * @param organization    the parent organization
     * @return  a list of direct sub organizations
     */
    List<Organization> getSubOrganizations(Organization organization);
}
