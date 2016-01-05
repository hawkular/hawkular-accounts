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
package org.hawkular.accounts.api.internal.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.hawkular.accounts.api.NamedRole;
import org.hawkular.accounts.api.OrganizationJoinRequestService;
import org.hawkular.accounts.api.OrganizationMembershipService;
import org.hawkular.accounts.api.OrganizationService;
import org.hawkular.accounts.api.PersonaService;
import org.hawkular.accounts.api.ResourceService;
import org.hawkular.accounts.api.internal.BoundStatements;
import org.hawkular.accounts.api.internal.NamedStatement;
import org.hawkular.accounts.api.model.JoinRequestStatus;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationJoinRequest;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.Resource;
import org.hawkular.accounts.api.model.Role;
import org.hawkular.accounts.api.model.Visibility;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

/**
 * @author Juraci Paixão Kröhling
 */
@Stateless
@PermitAll
public class OrganizationJoinRequestServiceImpl
        extends BaseServiceImpl<OrganizationJoinRequest>
        implements OrganizationJoinRequestService {

    MsgLogger logger = MsgLogger.LOGGER;

    @Inject
    ResourceService resourceService;

    @Inject
    OrganizationService organizationService;

    @Inject
    PersonaService personaService;

    @Inject
    OrganizationMembershipService membershipService;

    @Inject @NamedStatement(BoundStatements.JOIN_REQUEST_GET_BY_ID)
    Instance<BoundStatement> stmtGetById;

    @Inject @NamedStatement(BoundStatements.JOIN_REQUEST_CREATE)
    Instance<BoundStatement> stmtCreate;

    @Inject @NamedStatement(BoundStatements.JOIN_REQUEST_REMOVE)
    Instance<BoundStatement> stmtRemove;

    @Inject @NamedStatement(BoundStatements.JOIN_REQUEST_UPDATE_STATUS)
    Instance<BoundStatement> stmtUpdateStatus;

    @Inject @NamedStatement(BoundStatements.JOIN_REQUEST_LIST_BY_ORGANIZATION)
    Instance<BoundStatement> stmtListByOrganization;

    @Inject @NamedStatement(BoundStatements.JOIN_REQUEST_LIST_BY_PERSONA)
    Instance<BoundStatement> stmtListByPersona;

    @Inject
    @NamedRole("SuperUser")
    Role superUser;

    @Override
    public OrganizationJoinRequest getById(UUID uuid) {
        return getById(uuid, stmtGetById.get());
    }

    @Override
    public OrganizationJoinRequest create(Organization organization, Persona persona) {
        if (null == organization) {
            throw new IllegalArgumentException("Invalid organization (null)");
        }
        if (null == persona) {
            throw new IllegalArgumentException("Invalid persona (null)");
        }

        if (organization.getVisibility().equals(Visibility.PRIVATE)) {
            throw new IllegalArgumentException("This organization is private, users cannot apply to join.");
        }

        BoundStatement createStatement = stmtCreate.get();
        OrganizationJoinRequest joinRequest = new OrganizationJoinRequest(
                organization,
                persona,
                JoinRequestStatus.PENDING

        );

        Resource resource = resourceService.create(joinRequest.getId(), organization);
        resourceService.addRoleToPersona(resource, organization, superUser);

        bindBasicParameters(joinRequest, createStatement);
        createStatement.setUUID("organization", joinRequest.getOrganization().getIdAsUUID());
        createStatement.setUUID("persona", joinRequest.getPersona().getIdAsUUID());
        createStatement.setString("status", joinRequest.getStatus().name());
        session.execute(createStatement);
        logger.joinRequestCreated(
                joinRequest.getPersona().getId(),
                joinRequest.getOrganization().getId(),
                joinRequest.getId()
        );
        return joinRequest;
    }

    @Override
    public OrganizationJoinRequest accept(OrganizationJoinRequest request, Role role) {
        if (null == request) {
            throw new IllegalArgumentException("Invalid join request (null)");
        }
        if (null == role) {
            throw new IllegalArgumentException("Invalid role (null)");
        }

        if (!JoinRequestStatus.PENDING.equals(request.getStatus())) {
            throw new IllegalStateException("Only join requests in PENDING state can be accepted.");
        }

        request.setStatus(JoinRequestStatus.ACCEPTED);

        membershipService.create(
                request.getOrganization(),
                request.getPersona(),
                role
        );

        return update(request, stmtUpdateStatus.get().setString("status", request.getStatus().name()));
    }

    @Override
    public OrganizationJoinRequest reject(OrganizationJoinRequest request) {
        if (!JoinRequestStatus.PENDING.equals(request.getStatus())) {
            throw new IllegalStateException("Only join requests in PENDING state can be accepted.");
        }

        request.setStatus(JoinRequestStatus.REJECTED);
        return update(request, stmtUpdateStatus.get().setString("status", request.getStatus().name()));
    }

    @Override
    public void remove(OrganizationJoinRequest request) {
        session.execute(stmtRemove.get().setUUID("id", request.getIdAsUUID()));
    }

    @Override
    public List<OrganizationJoinRequest> getPendingRequestsForOrganization(Organization organization) {
        return getAllRequestsForOrganization(organization)
                .stream()
                .filter(r -> r.getStatus().equals(JoinRequestStatus.PENDING))
                .collect(Collectors.toList());
    }

    @Override
    public List<OrganizationJoinRequest> getAllRequestsForOrganization(Organization organization) {
        return getList(
                stmtListByOrganization
                        .get()
                        .setUUID("organization", organization.getIdAsUUID())
        );
    }

    @Override
    public List<OrganizationJoinRequest> getAllRequestsForPersona(Persona persona) {
        return getList(
                stmtListByPersona
                        .get()
                        .setUUID("persona", persona.getIdAsUUID())
        );
    }

    @Override
    OrganizationJoinRequest getFromRow(Row row) {
        OrganizationJoinRequest.Builder builder = new OrganizationJoinRequest.Builder();
        mapBaseFields(row, builder);

        return builder
                .organization(organizationService.getById(row.getUUID("organization")))
                .persona(personaService.getById(row.getUUID("persona")))
                .status(JoinRequestStatus.valueOf(row.getString("status")))
                .build();
    }
}
