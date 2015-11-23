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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.hawkular.accounts.api.NamedRole;
import org.hawkular.accounts.api.PersonaResourceRoleService;
import org.hawkular.accounts.api.PersonaService;
import org.hawkular.accounts.api.ResourceService;
import org.hawkular.accounts.api.internal.BoundStatements;
import org.hawkular.accounts.api.internal.NamedStatement;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.PersonaResourceRole;
import org.hawkular.accounts.api.model.Resource;
import org.hawkular.accounts.api.model.Role;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

/**
 * Main implementation of the {@link org.hawkular.accounts.api.ResourceService}. Consumers should get an instance of
 * this via CDI. This class should not be directly instantiated by the consumers.
 *
 * @author Juraci Paixão Kröhling
 */
@Stateless
@PermitAll
public class ResourceServiceImpl extends BaseServiceImpl<Resource> implements ResourceService {
    private static final Pattern UUID_PATTERN =
            Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");

    @Inject
    @NamedRole("SuperUser")
    Role superUser;

    @Inject
    PersonaResourceRoleService personaResourceRoleService;

    @Inject
    PersonaService personaService;

    @Inject @NamedStatement(BoundStatements.RESOURCE_GET_BY_ID)
    Instance<BoundStatement> stmtGetByIdInstance;

    @Inject @NamedStatement(BoundStatements.RESOURCE_GET_BY_PERSONA)
    Instance<BoundStatement> stmtGetByPersonaInstance;

    @Inject @NamedStatement(BoundStatements.RESOURCE_CREATE)
    Instance<BoundStatement> stmtCreateInstance;

    @Inject @NamedStatement(BoundStatements.RESOURCE_TRANSFER)
    Instance<BoundStatement> stmtTransferInstance;

    @Override
    public Resource getById(UUID id) {
        return getById(id, stmtGetByIdInstance.get());
    }

    @Override
    public Resource get(String id) {
        UUID uuid;
        if (!UUID_PATTERN.matcher(id).matches()) {
            // not an UUID, can't be a token
            uuid = UUID.nameUUIDFromBytes(id.getBytes());
        } else {
            uuid = UUID.fromString(id);
        }

        return getById(uuid);
    }

    @Override
    public Resource create(String id, Persona persona) {
        if (null == persona) {
            throw new IllegalArgumentException("The specified persona is invalid (null).");
        }
        return create(id, null, persona);
    }

    @Override
    public Resource create(String id, Resource parent) {
        if (null == parent) {
            throw new IllegalArgumentException("The given parent resource is invalid (null).");
        }
        return create(id, parent, null);
    }

    @Override
    public Resource create(String id, Resource parent, Persona persona) {
        BoundStatement stmtCreate = stmtCreateInstance.get();
        if (null == parent && null == persona) {
            throw new IllegalArgumentException("Either parent or persona should be provided when creating a resource");
        }

        Resource resource = new Resource(id, persona, parent);
        bindBasicParameters(resource, stmtCreate);

        if (null != persona) {
            stmtCreate.setUUID("persona", resource.getPersona().getIdAsUUID());
        } else {
            stmtCreate.setToNull("persona");
        }

        if (null != parent) {
            stmtCreate.setUUID("parent", resource.getParent().getIdAsUUID());
        } else {
            stmtCreate.setToNull("parent");
        }

        session.execute(stmtCreate);

        if (persona != null) {
            personaResourceRoleService.create(persona, resource, superUser);
        }

        return resource;
    }

    @Override
    public void delete(String id) {
        if (null == id) {
            throw new IllegalArgumentException("The given resource ID is invalid (null).");
        }

        Resource resource = get(id);
        if (resource != null) {
            personaResourceRoleService.getByResource(resource).stream().forEach(personaResourceRoleService::remove);
        }
    }

    @Override
    public List<Resource> getByPersona(Persona persona) {
        if (null == persona) {
            throw new IllegalArgumentException("The given persona is invalid (null).");
        }

        return getList(stmtGetByPersonaInstance.get().setUUID("persona", persona.getIdAsUUID()));
    }

    @Override
    public void transfer(Resource resource, Persona persona) {
        resource.setPersona(persona);
        update(resource, stmtTransferInstance.get().setUUID("persona", persona.getIdAsUUID()));
        revokeAllForPersona(resource, persona);
        addRoleToPersona(resource, persona, superUser);
    }

    @Override
    public void revokeAllForPersona(Resource resource, Persona persona) {
        personaResourceRoleService.getByPersonaAndResource(persona, resource)
                .stream()
                .forEach(personaResourceRoleService::remove);
    }

    @Override
    public PersonaResourceRole addRoleToPersona(Resource resource, Persona persona, Role role) {
        // do we have this combination already?
        List<PersonaResourceRole> existingList = personaResourceRoleService.getByPersonaAndResource(persona, resource)
                .stream()
                .filter(prr -> prr.getRole().equals(role))
                .collect(Collectors.toList());

        if (existingList.size() > 0) {
            return existingList.get(0);
        }

        // no, we don't have it, create one
        return personaResourceRoleService.create(persona, resource, role);
    }

    @Override
    public List<PersonaResourceRole> getRolesForPersona(Resource resource, Persona persona) {
        return personaResourceRoleService.getByPersonaAndResource(persona, resource);
    }

    @Override
    Resource getFromRow(Row row) {
        Resource parent = null;
        Persona persona = null;

        if (!row.isNull("parent")) {
            parent = getById(row.getUUID("parent"));
        }

        if (!row.isNull("persona")) {
            persona = personaService.getById(row.getUUID("persona"));
        }

        Resource.Builder builder = new Resource.Builder().parent(parent).persona(persona);
        mapBaseFields(row, builder);
        return builder.build();
    }
}
