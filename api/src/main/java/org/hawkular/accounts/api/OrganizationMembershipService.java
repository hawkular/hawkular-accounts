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

import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationMembership;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.Role;

/**
 * Service intended to manage the memberships that a {@link Persona} holds in an
 * {@link org.hawkular.accounts.api.model.Organization}. Can be injected via CDI into managed beans as follows:
 * <p>
 *     <pre>
 *         &#64;Inject OrganizationMembershipService organizationMembershipService;
 *     </pre>
 * </p>
 * Concrete implementations do not hold any state, but it's advised to get an instance through CDI or as an EJB.
 *
 * @author Juraci Paixão Kröhling
 */
public interface OrganizationMembershipService {
    /**
     * Retrieves the memberships that a given Persona has across all Organizations.
     * <br/><br/>
     * Example:<br/>
     * User "jdoe" is member of "Operations" with the role "SuperUser"<br/>
     * Organization "Operations" is a member of "Acme, Inc"<br/>
     * Only jdoe's membership as "SuperUser" on "Operations" is returned by this method.<br/>
     *
     * @param persona    the persona
     * @return the memberships of this persona across all organizations
     */
    List<OrganizationMembership> getMembershipsForPersona(Persona persona);

    /**
     * Retrieves the memberships for a given Organization.
     *
     * @param organization the organization
     * @return the memberships of this persona across all organizations
     */
    List<OrganizationMembership> getMembershipsForOrganization(Organization organization);

    /**
     * Retrieves the memberships for a given Organization.
     *
     * @param organization the organization
     * @return the memberships of this persona across all organizations
     */
    List<OrganizationMembership> getPersonaMembershipsForOrganization(Persona persona, Organization organization);

    /**
     * Retrieves an {@link OrganizationMembership} by its ID.
     * @param id    the ID of the organization membership
     * @return  the membership, or null if it's not found.
     */
    OrganizationMembership getMembershipById(String id);

    /**
     * Changes the membership and all related data so that the member only has the given role.
     *
     * @param membership    the membership to be changed
     * @param role          the new role for the membership.
     * @return  an updated membership, which might or not be the same as the original membership.
     */
    OrganizationMembership changeRole(OrganizationMembership membership, Role role);
}
