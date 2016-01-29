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

import org.hawkular.accounts.api.OrganizationMembershipService;
import org.hawkular.accounts.api.OrganizationService;
import org.hawkular.accounts.api.PersonaService;
import org.hawkular.accounts.api.ResourceService;
import org.hawkular.accounts.api.RoleService;
import org.hawkular.accounts.api.internal.BoundStatements;
import org.hawkular.accounts.api.internal.NamedStatement;
import org.hawkular.accounts.api.model.Member;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationMembership;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.Resource;
import org.hawkular.accounts.api.model.Role;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

/**
 * @author Juraci Paixão Kröhling
 */
@Stateless
@PermitAll
public class OrganizationMembershipServiceImpl
        extends BaseServiceImpl<OrganizationMembership>
        implements OrganizationMembershipService {
    MsgLogger logger = MsgLogger.LOGGER;

    @Inject
    ResourceService resourceService;

    @Inject
    PersonaService personaService;

    @Inject
    OrganizationService organizationService;

    @Inject
    RoleService roleService;

    @Inject @NamedStatement(BoundStatements.MEMBERSHIP_GET_BY_ID)
    Instance<BoundStatement> stmtGetByIdInstance;

    @Inject @NamedStatement(BoundStatements.MEMBERSHIP_GET_BY_ORGANIZATION)
    Instance<BoundStatement> stmtGetByOrganizationInstance;

    @Inject @NamedStatement(BoundStatements.MEMBERSHIP_GET_BY_PERSONA)
    Instance<BoundStatement> stmtGetByPersonaInstance;

    @Inject @NamedStatement(BoundStatements.MEMBERSHIP_REMOVE)
    Instance<BoundStatement> stmtRemoveInstance;

    @Inject @NamedStatement(BoundStatements.MEMBERSHIP_CREATE)
    Instance<BoundStatement> stmtCreateInstance;

    @Inject @NamedStatement(BoundStatements.MEMBERSHIP_CHANGE_ROLE)
    Instance<BoundStatement> stmtChangeRoleInstance;

    @Override
    public OrganizationMembership create(Organization organization, Persona persona, Role role) {
        BoundStatement stmtCreate = stmtCreateInstance.get();
        OrganizationMembership membership = new OrganizationMembership(organization, persona, role);

        bindBasicParameters(membership, stmtCreate);
        stmtCreate.setUUID("member", membership.getMember().getIdAsUUID());
        stmtCreate.setUUID("organization", membership.getOrganization().getIdAsUUID());
        stmtCreate.setUUID("role", membership.getRole().getIdAsUUID());
        session.execute(stmtCreate);

        // for permission checking
        Resource resource = resourceService.getById(organization.getIdAsUUID());
        resourceService.addRoleToPersona(resource, persona, role);
        logger.organizationMembershipCreated(membership.getId());
        return membership;
    }

    @Override
    public List<OrganizationMembership> getMembershipsForPersona(Persona persona) {
        return getList(stmtGetByPersonaInstance.get().setUUID("member", persona.getIdAsUUID()));
    }

    @Override
    public List<OrganizationMembership> getMembershipsForOrganization(Organization organization) {
        return getList(stmtGetByOrganizationInstance.get().setUUID("organization", organization.getIdAsUUID()));
    }

    @Override
    public List<OrganizationMembership> getPersonaMembershipsForOrganization(Persona persona,
                                                                                       Organization organization) {
        return getMembershipsForPersona(persona)
                .stream()
                .filter(m -> m.getOrganization().equals(organization))
                .collect(Collectors.toList());
    }

    @Override
    public OrganizationMembership getMembershipById(String id) {
        return getById(UUID.fromString(id));
    }

    @Override
    public OrganizationMembership getById(UUID id) {
        if (null == id) {
            throw new IllegalArgumentException("The given membership ID is invalid (null).");
        }

        return getById(id, stmtGetByIdInstance.get());
    }

    @Override
    public OrganizationMembership changeRole(OrganizationMembership membership, Role role) {
        BoundStatement stmtChangeRole = stmtChangeRoleInstance.get();
        membership.setRole(role);
        stmtChangeRole.setUUID("role", membership.getRole().getIdAsUUID());
        update(membership, stmtChangeRole);

        // the code above was for "organization" data. the code below is for RBAC.
        // for now, we allow only one role for each organization, so, revoke all current roles and add the given role
        Persona persona = personaService.getById(membership.getMember().getIdAsUUID());
        Resource resource = resourceService.getById(membership.getOrganization().getIdAsUUID());
        resourceService.revokeAllForPersona(resource, persona);
        resourceService.addRoleToPersona(resource, persona, role);
        logger.organizationMembershipRoleChanged(membership.getId(), role.getName());
        return membership;
    }

    @Override
    public void remove(OrganizationMembership organizationMembership) {
        remove(organizationMembership.getIdAsUUID());
    }

    @Override
    public void remove(UUID id) {
        session.execute(stmtRemoveInstance.get().setUUID("id", id));
    }

    @Override
    OrganizationMembership getFromRow(Row row) {
        Organization organization = organizationService.getById(row.getUUID("organization"));
        Member member = personaService.getById(row.getUUID("member"));
        Role role = roleService.getById(row.getUUID("role"));
        OrganizationMembership.Builder builder = new OrganizationMembership.Builder();
        mapBaseFields(row, builder);
        return builder.organization(organization).member(member).role(role).build();
    }
}
