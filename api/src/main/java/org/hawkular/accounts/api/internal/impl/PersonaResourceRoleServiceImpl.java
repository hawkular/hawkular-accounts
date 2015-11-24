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

import org.hawkular.accounts.api.PersonaResourceRoleService;
import org.hawkular.accounts.api.PersonaService;
import org.hawkular.accounts.api.ResourceService;
import org.hawkular.accounts.api.RoleService;
import org.hawkular.accounts.api.internal.BoundStatements;
import org.hawkular.accounts.api.internal.NamedStatement;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.PersonaResourceRole;
import org.hawkular.accounts.api.model.Resource;
import org.hawkular.accounts.api.model.Role;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

/**
 * @author Juraci Paixão Kröhling
 */
@Stateless
@PermitAll
public class PersonaResourceRoleServiceImpl
        extends BaseServiceImpl<PersonaResourceRole>
        implements PersonaResourceRoleService {

    @Inject
    PersonaService personaService;

    @Inject
    ResourceService resourceService;

    @Inject
    RoleService roleService;

    @Inject @NamedStatement(BoundStatements.PRR_GET_BY_ID)
    Instance<BoundStatement> stmtGetByIdInstance;

    @Inject @NamedStatement(BoundStatements.PRR_GET_BY_PERSONA)
    Instance<BoundStatement> stmtGetByPersonaInstance;

    @Inject @NamedStatement(BoundStatements.PRR_GET_BY_RESOURCE)
    Instance<BoundStatement> stmtGetByResourceInstance;

    @Inject @NamedStatement(BoundStatements.PRR_CREATE)
    Instance<BoundStatement> stmtCreateInstance;

    @Inject @NamedStatement(BoundStatements.PRR_REMOVE)
    Instance<BoundStatement> stmtRemoveInstance;

    @Override
    PersonaResourceRole getFromRow(Row row) {
        Persona persona = personaService.getById(row.getUUID("persona"));
        Resource resource = resourceService.getById(row.getUUID("resource"));
        Role role = roleService.getById(row.getUUID("role"));

        PersonaResourceRole.Builder builder = new PersonaResourceRole.Builder();
        mapBaseFields(row, builder);
        return builder.persona(persona).resource(resource).role(role).build();
    }

    @Override
    public PersonaResourceRole getById(UUID id) {
        return getById(id, stmtGetByIdInstance.get());
    }

    @Override
    public PersonaResourceRole create(Persona persona, Resource resource, Role role) {
        BoundStatement stmtCreate = stmtCreateInstance.get();
        PersonaResourceRole prr = new PersonaResourceRole(persona, role, resource);
        bindBasicParameters(prr, stmtCreate);
        stmtCreate.setUUID("persona", persona.getIdAsUUID());
        stmtCreate.setUUID("resource", resource.getIdAsUUID());
        stmtCreate.setUUID("role", role.getIdAsUUID());
        session.execute(stmtCreate);
        return prr;
    }

    @Override
    public void remove(UUID id) {
        session.execute(stmtRemoveInstance.get().setUUID("id", id));
    }

    @Override
    public void remove(PersonaResourceRole personaResourceRole) {
        remove(personaResourceRole.getIdAsUUID());
    }

    @Override
    public List<PersonaResourceRole> getByPersona(Persona persona) {
        return getList(stmtGetByPersonaInstance.get().setUUID("persona", persona.getIdAsUUID()));
    }

    @Override
    public List<PersonaResourceRole> getByPersonaAndResource(Persona persona, Resource resource) {
        return getByPersona(persona)
                .stream()
                .filter(prr -> prr.getResource().equals(resource))
                .collect(Collectors.toList());
    }

    @Override
    public List<PersonaResourceRole> getByResource(Resource resource) {
        return getList(stmtGetByResourceInstance.get().setUUID("resource", resource.getIdAsUUID()));
    }
}
