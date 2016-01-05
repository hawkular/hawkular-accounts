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
import org.hawkular.accounts.api.model.OrganizationJoinRequest;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.Role;

/**
 * @author Juraci Paixão Kröhling
 */
public interface OrganizationJoinRequestService {

    /**
     * Retrieve a specific Join Request based on its ID
     * @return  the OrganizationJoinRequest
     */
    OrganizationJoinRequest getById(UUID uuid);

    /**
     * Creates a new Join Request on the given Organization by the given Persona
     * @param organization    the organization the persona wants to join
     * @param persona         the persona who wants to join the organization
     * @return  a newly created OrganizationJoinRequest
     */
    OrganizationJoinRequest create(Organization organization, Persona persona);

    /**
     * Accepts a previously created join request
     * @param request    the join request that is being accepted
     * @return  the join request with the new status
     */
    OrganizationJoinRequest accept(OrganizationJoinRequest request, Role role);

    /**
     * Rejects a previously created join request
     * @param request    the join request that is being rejected
     * @return  the join request with the new status
     */
    OrganizationJoinRequest reject(OrganizationJoinRequest request);

    /**
     * Removes a join request from the permanent storage
     * @param request    the join request to remove
     */
    void remove(OrganizationJoinRequest request);

    /**
     * Lists the PENDING join requests for the given organization
     * @param organization    the organization to list the pending requests
     * @return  a List of OrganizationJoinRequest with PENDING as status
     */
    List<OrganizationJoinRequest> getPendingRequestsForOrganization(Organization organization);

    /**
     * Lists all join requests for the given organization, no matter the status
     * @param organization    the organization to list the requests
     * @return  a List of OrganizationJoinRequest
     */
    List<OrganizationJoinRequest> getAllRequestsForOrganization(Organization organization);

    /**
     * Lists all join requests for the given persona, no matter the status
     * @param persona   the persona
     * @return  a List of OrganizationJoinRequest
     */
    List<OrganizationJoinRequest> getAllRequestsForPersona(Persona persona);
}
