--
-- Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
-- and other contributors as indicated by the @author tags.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--    http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

-- #

CREATE KEYSPACE IF NOT EXISTS hawkular_accounts WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

-- #

CREATE TABLE IF NOT EXISTS hawkular_accounts.users (
    id uuid,
    name text,
    email text,
    createdAt timestamp,
    updatedAt timestamp,
    PRIMARY KEY (id)
);

-- #

CREATE TABLE IF NOT EXISTS hawkular_accounts.organizations (
    id uuid,
    owner uuid,
    name text,
    description text,
    visibility text,
    createdAt timestamp,
    updatedAt timestamp,
    PRIMARY KEY (id)
);

-- #

CREATE INDEX IF NOT EXISTS ON hawkular_accounts.organizations (owner);

-- #

CREATE INDEX IF NOT EXISTS ON hawkular_accounts.organizations (name);

-- #

CREATE INDEX IF NOT EXISTS ON hawkular_accounts.organizations (visibility);

-- #

CREATE TABLE IF NOT EXISTS hawkular_accounts.organization_memberships (
    id uuid,
    organization uuid,
    member uuid,
    role uuid,
    createdAt timestamp,
    updatedAt timestamp,
    PRIMARY KEY (id)
);

-- #

CREATE INDEX IF NOT EXISTS ON hawkular_accounts.organization_memberships (organization);

-- #

CREATE INDEX IF NOT EXISTS ON hawkular_accounts.organization_memberships (member);

-- #

CREATE TABLE IF NOT EXISTS hawkular_accounts.roles (
    id uuid,
    name text,
    description text,
    createdAt timestamp,
    updatedAt timestamp,
    PRIMARY KEY (id)
);

-- #

CREATE INDEX IF NOT EXISTS ON hawkular_accounts.roles (name);

-- #

CREATE TABLE IF NOT EXISTS hawkular_accounts.operations (
    id uuid,
    name text,
    createdAt timestamp,
    updatedAt timestamp,
    PRIMARY KEY (id)
);

-- #

CREATE INDEX IF NOT EXISTS ON hawkular_accounts.operations (name);

-- #

CREATE TABLE IF NOT EXISTS hawkular_accounts.permissions (
    id uuid,
    operation uuid,
    role uuid,
    createdAt timestamp,
    updatedAt timestamp,
    PRIMARY KEY (id)
);

-- #

CREATE INDEX IF NOT EXISTS ON hawkular_accounts.permissions (operation);

-- #

CREATE TABLE IF NOT EXISTS hawkular_accounts.invitations (
    id uuid,
    email text,
    invitedBy uuid,
    acceptedBy uuid,
    organization uuid,
    role uuid,
    acceptedAt timestamp,
    dispatchedAt timestamp,
    createdAt timestamp,
    updatedAt timestamp,
    PRIMARY KEY (id)
);

-- #

CREATE INDEX IF NOT EXISTS ON hawkular_accounts.invitations (organization);

-- #

CREATE TABLE IF NOT EXISTS hawkular_accounts.resources (
    id uuid,
    persona uuid,
    parent uuid,
    createdAt timestamp,
    updatedAt timestamp,
    PRIMARY KEY (id)
);

-- #

CREATE INDEX IF NOT EXISTS ON hawkular_accounts.resources (persona);

-- #

CREATE INDEX IF NOT EXISTS ON hawkular_accounts.resources (parent);

-- #

CREATE TABLE IF NOT EXISTS hawkular_accounts.persona_resource_roles (
    id uuid,
    persona uuid,
    resource uuid,
    role uuid,
    createdAt timestamp,
    updatedAt timestamp,
    PRIMARY KEY (id)
);

-- #

CREATE INDEX IF NOT EXISTS ON hawkular_accounts.persona_resource_roles (persona);

-- #

CREATE INDEX IF NOT EXISTS ON hawkular_accounts.persona_resource_roles (resource);

-- #

CREATE TABLE IF NOT EXISTS hawkular_accounts.user_settings (
    id uuid,
    persona uuid,
    properties map<text, text>,
    createdAt timestamp,
    updatedAt timestamp,
    PRIMARY KEY (id)
);

-- #

CREATE INDEX IF NOT EXISTS ON hawkular_accounts.user_settings (persona);

-- #

CREATE TABLE IF NOT EXISTS hawkular_accounts.join_requests (
    id uuid,
    organization uuid,
    persona uuid,
    status text,
    createdAt timestamp,
    updatedAt timestamp,
    PRIMARY KEY (id)
);

-- #

CREATE INDEX IF NOT EXISTS ON hawkular_accounts.join_requests (organization);

-- #

CREATE INDEX IF NOT EXISTS ON hawkular_accounts.join_requests (status);

-- #

CREATE INDEX IF NOT EXISTS ON hawkular_accounts.join_requests (persona);
