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
package org.hawkular.accounts.api.internal.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.hawkular.accounts.api.InvitationService;
import org.hawkular.accounts.api.NamedRole;
import org.hawkular.accounts.api.OrganizationMembershipService;
import org.hawkular.accounts.api.OrganizationService;
import org.hawkular.accounts.api.PersonaService;
import org.hawkular.accounts.api.ResourceService;
import org.hawkular.accounts.api.internal.BoundStatements;
import org.hawkular.accounts.api.internal.NamedStatement;
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
public class OrganizationServiceImpl extends BaseServiceImpl<Organization> implements OrganizationService {
    @Inject
    OrganizationMembershipService membershipService;

    @Inject
    ResourceService resourceService;

    @Inject
    InvitationService invitationService;

    @Inject
    PersonaService personaService;

    @Inject
    @NamedRole("SuperUser")
    Role superUser;

    @Inject @NamedStatement(BoundStatements.ORGANIZATION_GET_BY_ID)
    Instance<BoundStatement> stmtGetByIdInstance;

    @Inject @NamedStatement(BoundStatements.ORGANIZATION_GET_BY_NAME)
    Instance<BoundStatement> stmtGetByNameInstance;

    @Inject @NamedStatement(BoundStatements.ORGANIZATION_GET_BY_OWNER)
    Instance<BoundStatement> stmtGetByOwnerInstance;

    @Inject @NamedStatement(BoundStatements.ORGANIZATION_CREATE)
    Instance<BoundStatement> stmtCreateInstance;

    @Inject @NamedStatement(BoundStatements.ORGANIZATION_REMOVE)
    Instance<BoundStatement> stmtRemoveInstance;

    @Inject @NamedStatement(BoundStatements.ORGANIZATION_TRANSFER)
    Instance<BoundStatement> stmtTransferInstance;

    @Override
    public Organization getById(UUID id) {
        if (null == id) {
            throw new IllegalArgumentException("The given organization ID is invalid (null).");
        }

        return getById(id, stmtGetByIdInstance.get());
    }

    @Override
    public Organization getByName(String name) {
        BoundStatement stmtGetByName = stmtGetByNameInstance.get();
        if (null == name) {
            throw new IllegalArgumentException("The given organization name is invalid (null).");
        }

        stmtGetByName.setString("name", name);
        return getSingleRecord(stmtGetByName);
    }

    @Override
    public Organization get(String id) {
        return getById(UUID.fromString(id));
    }

    @Override
    public List<Organization> getOrganizationsForPersona(Persona persona) {
        return getOrganizationsFromMemberships(membershipService.getMembershipsForPersona(persona));
    }

    @Override
    public List<Organization> getOrganizationsFromMemberships(List<OrganizationMembership> memberships) {
        return memberships
                .stream()
                .map(OrganizationMembership::getOrganization)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Organization createOrganization(String name, String description, Persona owner) {
        BoundStatement stmtCreate = stmtCreateInstance.get();
        if (null != getByName(name)) {
            throw new IllegalArgumentException("There's already an organization with this name.");
        }

        Organization organization = new Organization(owner);
        organization.setName(name);
        organization.setDescription(description);

        // this is for the permission checker itself
        Resource resource = resourceService.create(organization.getId(), owner);
        resourceService.addRoleToPersona(resource, owner, superUser);

        // this is for our organization management
        bindBasicParameters(organization, stmtCreate);
        stmtCreate.setString("name", organization.getName());
        stmtCreate.setString("description", organization.getDescription());
        stmtCreate.setUUID("owner", organization.getOwner().getIdAsUUID());
        session.execute(stmtCreate);
        membershipService.create(organization, owner, superUser);
        return organization;
    }

    @Override
    public void deleteOrganization(Organization organization) {
        Resource resource = resourceService.get(organization.getId());

        invitationService.getInvitationsForOrganization(organization).stream().forEach(invitation -> {
            if (null != invitation.getAcceptedBy()) {
                resourceService.revokeAllForPersona(resource, invitation.getAcceptedBy());
            }
            invitationService.remove(invitation);
        });

        membershipService.getMembershipsForOrganization(organization).stream().forEach(membershipService::remove);
        resourceService.revokeAllForPersona(resource, organization.getOwner());
        resourceService.delete(organization.getId());
        session.execute(stmtRemoveInstance.get().setUUID("id", organization.getIdAsUUID()));
    }

    @Override
    public void transfer(Organization organization, Persona newOwner) {
        BoundStatement stmtTransfer = stmtTransferInstance.get();
        // first, we remove all the current memberships of the new owner, as it will now be super user
        membershipService.getPersonaMembershipsForOrganization(newOwner, organization)
                .stream()
                .forEach(membershipService::remove);

        // now, we add as super user
        membershipService.create(organization, newOwner, superUser);

        // we change the Resource's owner
        Resource resource = resourceService.getById(organization.getIdAsUUID());
        resourceService.transfer(resource, newOwner);

        // and finally, we change the owner on the organization
        organization.setOwner(newOwner);
        stmtTransfer.setUUID("owner", organization.getOwner().getIdAsUUID());
        session.execute(stmtTransfer);
    }

    @Override
    public List<Organization> getSubOrganizations(Organization organization) {
        return getList(stmtGetByOwnerInstance.get().setUUID("owner", organization.getIdAsUUID()));
    }

    @Override
    Organization getFromRow(Row row) {
        Persona owner = personaService.getById(row.getUUID("owner"));
        String name = row.getString("name");
        String description = row.getString("description");

        Organization.Builder builder = new Organization.Builder();
        mapBaseFields(row, builder);
        return builder.owner(owner).name(name).description(description).build();
    }
}
