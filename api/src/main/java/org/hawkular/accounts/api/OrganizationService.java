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

import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationMembership;
import org.hawkular.accounts.api.model.Persona;

import java.util.List;

/**
 * Service intended to handle data related to an {@link Organization}. Can be injected via CDI into managed beans as
 * follows:
 * <p>
 *     <pre>
 *         &#64;Inject OrganizationService organizationService;
 *     </pre>
 * </p>
 * Concrete implementations do not hold any state, but it's advised to get an instance through CDI or as an EJB.
 *
 * @author Juraci Paixão Kröhling
 */
public interface OrganizationService {
    /**
     * Retrieves the Organizations to which a given Persona directly is member/owner of.
     * <br/><br/>
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
}
