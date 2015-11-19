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
package org.hawkular.accounts.api.internal;

/**
 * Lists all the possible statements we have for this application.
 *
 * @author Juraci Paixão Kröhling
 */
public enum BoundStatements {
    // default statement, for the producer... we need this, as the annotation requires a default value, and we don't
    // want any default, so, we assign an "invalid" one as default.
    DEFAULT(null),

    // Role statements
    ROLES_GET_BY_ID(
            "SELECT * FROM hawkular_accounts.roles WHERE id = :id"
    ),

    ROLES_GET_BY_NAME(
            "SELECT * FROM hawkular_accounts.roles WHERE name = :name"
    ),

    ROLES_CREATE(
            "INSERT INTO hawkular_accounts.roles " +
            "  (id, name, description, createdAt, updatedAt) " +
            "VALUES " +
            "  (:id, :name, :description, :createdAt, :updatedAt)"
    ),

    // Invitation statements
    INVITATION_GET_BY_TOKEN(
            "SELECT * FROM hawkular_accounts.invitations WHERE id = :id"
    ),

    INVITATIONS_GET_BY_ORGANIZATION(
            "SELECT * FROM hawkular_accounts.invitations WHERE organization = :organization"
    ),

    INVITATIONS_CREATE(
            "INSERT INTO hawkular_accounts.invitations " +
            " (id, email, invitedBy, organization, role, createdAt, updatedAt) " +
            " VALUES " +
            " (:id, :email, :invitedBy, :organization, :role, :createdAt, :updatedAt)"
    ),

    INVITATIONS_DELETE(
            "DELETE FROM hawkular_accounts.invitations WHERE id = :id"
    ),

    INVITATIONS_ACCEPT(
            "UPDATE " +
            "  hawkular_accounts.invitations " +
            "SET " +
            "  acceptedAt = :acceptedAt, " +
            "  updatedAt = :updatedAt " +
            "WHERE " +
            "  id = :id"
    ),

    INVITATIONS_DISPATCH(
            "UPDATE " +
            "  hawkular_accounts.invitations " +
            "SET " +
            "  dispatchedAt = :dispatchedAt, " +
            "  updatedAt = :updatedAt " +
            "WHERE id = :id"
    ),

    // User statements
    USER_GET_BY_ID(
            "SELECT * FROM hawkular_accounts.users WHERE id = :id"
    ),

    USER_CREATE(
            "INSERT INTO hawkular_accounts.users " +
            "  (id, createdAt, updatedAt, name) " +
            "VALUES " +
            "  (:id, :createdAt, :updatedAt, :name);"
    ),

    USER_UPDATE(
            "UPDATE hawkular_accounts.users " +
            "SET updatedAt = :updatedAt, name = :name " +
            "WHERE id = :id"
    ),

    USER_ALL("SELECT * FROM hawkular_accounts.users"),

    // Permission statements
    PERMISSION_GET_BY_ID(
            "SELECT * FROM hawkular_accounts.permissions WHERE id = :id"
    ),

    PERMISSION_DELETE(
            "DELETE FROM hawkular_accounts.permissions WHERE id = :id"
    ),

    PERMISSION_CREATE(
            "INSERT INTO hawkular_accounts.permissions " +
            "(id, operation, role, createdAt, updatedAt) " +
            "VALUES " +
            "(:id, :operation, :role, :createdAt, :updatedAt)"
    ),

    PERMISSIONS_GET_BY_OPERATION(
            "SELECT * FROM hawkular_accounts.permissions WHERE operation = :operation"
    ),

    // Operation statements
    OPERATION_GET_BY_NAME("SELECT * FROM hawkular_accounts.operations WHERE name = :name"),
    OPERATION_GET_BY_ID("SELECT * FROM hawkular_accounts.operations WHERE id = :id"),

    OPERATION_CREATE(
            "INSERT INTO hawkular_accounts.operations " +
            "  (id, name, createdAt, updatedAt) " +
            "VALUES " +
            "  (:id, :name, :createdAt, :updatedAt)"
    ),

    // Resources statements
    RESOURCE_GET_BY_ID("SELECT * FROM hawkular_accounts.resources WHERE id = :id"),
    RESOURCE_GET_BY_PERSONA("SELECT * FROM hawkular_accounts.resources WHERE persona = :persona"),
    RESOURCE_TRANSFER(
            "UPDATE hawkular_accounts.resources SET persona = :persona, updatedAt = :updatedAt WHERE id = :id"
    ),
    RESOURCE_CREATE(
            "INSERT INTO hawkular_accounts.resources " +
            " (id, persona, parent, createdAt, updatedAt) " +
            "VALUES " +
            " (:id, :persona, :parent, :createdAt, :updatedAt)"
    ),

    // PersonaResourceRole statements
    PRR_GET_BY_ID("SELECT * FROM hawkular_accounts.persona_resource_roles WHERE id = :id"),
    PRR_GET_BY_RESOURCE("SELECT * FROM hawkular_accounts.persona_resource_roles WHERE resource = :resource"),
    PRR_GET_BY_PERSONA("SELECT * FROM hawkular_accounts.persona_resource_roles WHERE persona = :persona"),
    PRR_REMOVE("DELETE FROM hawkular_accounts.persona_resource_roles WHERE id = :id"),
    PRR_CREATE(
            "INSERT INTO hawkular_accounts.persona_resource_roles " +
            " (id, persona, resource, role, createdAt, updatedAt) " +
            "VALUES " +
            " (:id, :persona, :resource, :role, :createdAt, :updatedAt)"
    ),

    // Organization statements
    ORGANIZATION_GET_BY_ID("SELECT * FROM hawkular_accounts.organizations WHERE id = :id"),
    ORGANIZATION_GET_BY_NAME("SELECT * FROM hawkular_accounts.organizations WHERE name = :name"),
    ORGANIZATION_GET_BY_OWNER("SELECT * FROM hawkular_accounts.organizations WHERE owner = :owner"),
    ORGANIZATION_REMOVE("DELETE FROM hawkular_accounts.organizations WHERE id = :id"),
    ORGANIZATION_CREATE(
            "INSERT INTO hawkular_accounts.organizations" +
            " (id, owner, name, description, createdAt, updatedAt) " +
            "VALUES " +
            " (:id, :owner, :name, :description, :createdAt, :updatedAt)"
    ),
    ORGANIZATION_TRANSFER(
            "UPDATE hawkular_accounts.organizations SET owner = :owner, updatedAt = :updatedAt WHERE id = :id"
    ),

    // Organization memberships
    MEMBERSHIP_GET_BY_ID("SELECT * FROM hawkular_accounts.organization_memberships WHERE id = :id"),
    MEMBERSHIP_GET_BY_ORGANIZATION(
            "SELECT * FROM hawkular_accounts.organization_memberships WHERE organization = :organization"),
    MEMBERSHIP_GET_BY_PERSONA(
            "SELECT * FROM hawkular_accounts.organization_memberships WHERE member = :member"
    ),
    MEMBERSHIP_REMOVE("DELETE FROM hawkular_accounts.organization_memberships WHERE id = :id"),
    MEMBERSHIP_CREATE(
            "INSERT INTO hawkular_accounts.organization_memberships " +
            " (id, organization, member, role, createdAt, updatedAt) " +
            " VALUES " +
            " (:id, :organization, :member, :role, :createdAt, :updatedAt)"
    ),
    MEMBERSHIP_CHANGE_ROLE(
            "UPDATE hawkular_accounts.organization_memberships SET role = :role, updatedAt = :updatedAt WHERE id = :id"
    ),

    // User settings
    SETTINGS_GET_BY_ID("SELECT * FROM hawkular_accounts.user_settings WHERE id = :id"),
    SETTINGS_GET_BY_USER("SELECT * FROM hawkular_accounts.user_settings WHERE persona = :persona"),
    SETTINGS_UPDATE(
            "UPDATE hawkular_accounts.user_settings SET properties = :properties, updatedAt = :updatedAt WHERE id = :id"
    ),
    SETTINGS_CREATE(
            "INSERT INTO hawkular_accounts.user_settings" +
            " (id, persona, createdAt, updatedAt) " +
            "VALUES " +
            " (:id, :persona, :createdAt, :updatedAt) "
    ),

    ;

    private String value;

    BoundStatements(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
