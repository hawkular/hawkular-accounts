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

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.ValidIdRange;

/**
 * JBoss Logging integration, with the possible messages that we have for the API.
 *
 * @author Juraci Paixão Kröhling
 */
@MessageLogger(projectCode = "HAWKACC")
@ValidIdRange(min = 100000, max = 109999)
public interface MsgLogger extends BasicLogger {
    MsgLogger LOGGER = Logger.getMessageLogger(MsgLogger.class, MsgLogger.class.getPackage().getName());

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 100000, value = "Could not process prepare query: [%s]")
    void couldNotPrepareQuery(String query, @Cause Throwable t);

    @LogMessage(level = Logger.Level.FATAL)
    @Message(id = 100001, value = "Failed to initialize Cassandra's schema for Accounts. Reason")
    void failedToInitializeSchema(@Cause Throwable t);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 100002, value = "Shutting down Cassandra driver for Accounts")
    void shuttingDownCassandraDriver();

    @LogMessage(level = Logger.Level.FATAL)
    @Message(id = 100003, value = "Failed to properly shutdown the Cassandra driver for Accounts. Reason")
    void failedToShutdownDriver(@Cause Throwable t);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 100004, value = "Organization [%s] created. ID: [%s]")
    void organizationCreated(String organizationName, String organizationId);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 100005, value = "Join request from [%s] to join organization [%s] created. ID: [%s]")
    void joinRequestCreated(String userId, String organizationId, String joinRequestId);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 100006, value = "There's an incoming request for an non-secure location, but a component has " +
            "requested information about the current persona. This probably indicates a mismatch in the integration " +
            "of the component. Either the component should accept non-secure requests and not ask for a Persona, or " +
            "should only accept secure requests. Hawkular Accounts is returning NULL to the component. Lookout for " +
            "NullPointerExceptions!")
    void nonAuthRequestWantsPersona();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100007, value = "Invitation [%s] created.")
    void invitationCreated(String invitationId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100008, value = "Invitation [%s] accepted.")
    void invitationAccepted(String invitationId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100009, value = "Invitation [%s] removed.")
    void invitationRemoved(String invitationId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100010, value = "Invitation [%s] dispatched.")
    void invitationDispatched(String invitationId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100011, value = "There's already an operation with the name [%s].")
    void duplicateOperation(String operationName);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100012, value = "Operation [%s] created.")
    void operationCreated(String operationName);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 100013, value = "Operation [%s] has been changed. Persisting new data.")
    void operationHasChanged(String operationName);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100014, value = "Join Request [%s] accepted.")
    void joinRequestAccepted(String joinRequestId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100015, value = "Join Request [%s] rejected.")
    void joinRequestRejected(String joinRequestId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100016, value = "Join Request [%s] removed.")
    void joinRequestRemoved(String joinRequestId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100017, value = "Organization membership [%s] created.")
    void organizationMembershipCreated(String organizationMembershipId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100018, value = "Organization membership [%s] has its role changed to [%s].")
    void organizationMembershipRoleChanged(String organizationMembershipId, String roleName);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100019, value = "Persona [%s] is a member of [%d] organizations.")
    void organizationsPersonaJoined(String personaId, int organizationsJoined);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100020, value = "Persona [%s] can join [%d] organizations.")
    void organizationsPersonaToJoin(String personaId, int organizationsToJoin);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100021, value = "Persona [%s] has [%d] join requests.")
    void personaJoinRequests(String personaId, int joinRequests);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100022, value = "Persona [%s] has [%d] pending join requests.")
    void personaJoinRequestsPending(String personaId, int joinRequests);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100023, value = "Persona [%s] can join [%d] organizations, removing the ones that are pending a " +
            "decision.")
    void organizationsPersonaToJoinFiltered(String personaId, int joinRequests);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100024, value = "Starting removal of organization [%s]")
    void startingRemovalOfOrganization(String organizationId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100025, value = "Finished removal of organization [%s]")
    void finishedRemovalOfOrganization(String organizationId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100026, value = "Starting transfer of organization [%s] from [%s] to [%s]")
    void startingTransferOfOrganization(String organizationId, String oldOwnerId, String newOwnerId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100027, value = "Finished transfer of organization [%s] from [%s] to [%s]")
    void finishedTransferOfOrganization(String organizationId, String oldOwnerId, String newOwnerId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100028, value = "Checking if the persona [%s] has permission to perform [%s] on resource [%s]")
    void checkPermission(String personaId, String operationName, String resourceId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100029, value = "The resource [%s] has no explicit owner. Checking the parent's: [%s]")
    void checkingParentsPermission(String resourceId, String parentsId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100030, value = "Permission to perform [%s] on resource [%s] to persona [%s] granted because the " +
            "persona is the owner of the resource.")
    void permissionGrantedToOwner(String operationName, String resourceId, String personaId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100031, value = "The operation [%s] can be performed by personas in [%d] different roles.")
    void operationPermittedToRoles(String operationName, int numOfRoles);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100032, value = "The persona [%s] has [%d] different roles.")
    void personaHasRoles(String personaId, int numOfRoles);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100033, value = "Result of check if the persona [%s] has permission to perform [%s] on resource " +
            "[%s]: [%b]")
    void checkPermissionResult(String personaId, String operationName, String resourceId, boolean allowed);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100034, value = "Permission [%s] created for operation [%s] with role [%s]")
    void permissionCreated(String permissionId, String operationName, String roleName);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100035, value = "Permission [%s] removed for operation [%s] with role [%s]")
    void permissionRemoved(String permissionId, String operationName, String roleName);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100036, value = "Created link between persona [%s], resource [%s] and role [%s].")
    void personaResourceRoleCreated(String personaId, String resourceId, String roleName);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100037, value = "PersonaResourceRole ID [%s] removed.")
    void personaResourceRoleRemoved(String prrId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100038, value = "Removed link between persona [%s], resource [%s] and role [%s].")
    void personaResourceRoleRemoved(String personaId, String resourceId, String roleName);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100039, value = "Determining effective roles for persona [%s] on resource [%s].")
    void determiningEffectiveRolesForPersonaOnResource(String personaId, String resourceId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100040, value = "Persona [%s] on resource [%s]: [%d] roles.")
    void numOfDirectRolesOnResource(String personaId, String resourceId, int numRoles);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100041, value = "Persona [%s] on resource [%s] has no direct roles. Checking indirect roles.")
    void noDirectRolesOnResource(String personaId, String resourceId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100042, value = "Checking roles for persona [%s] on resource [%s] via organization [%s]")
    void checkingIndirectRolesViaOrganization(String personaId, String resourceId, String organizationId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100043, value = "Persona [%s] on resource [%s] via organization [%s]: [%d] roles.")
    void numOfEffectiveRolesViaOrganization(String personaId, String resourceId, String organizationId, int numRoles);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100044, value = "Total of roles for persona [%s] on resource [%s]: [%d] roles.")
    void totalEffectiveRolesOnResource(String personaId, String resourceId, int numRoles);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100045, value = "Total of roles (including implicit) for persona [%s] on resource [%s]: [%d] roles.")
    void totalEffectiveRolesOnResourceWithImplicitRoles(String personaId, String resourceId, int numRoles);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100046, value = "The provided resource ID [%s] isn't an UUID. Derived UUID from the ID: [%s].")
    void resourceIdIsntUUID(String resourceId, String uuid);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100047, value = "Resource [%s] created.")
    void resourceCreated(String resourceId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100048, value = "Resource [%s] is being created with persona [%s] as owner.")
    void resourceBeingCreatedWithPersona(String resourceId, String personaId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100049, value = "Resource [%s] is being created with resource [%s] as parent.")
    void resourceBeingCreatedWithParent(String resourceId, String parentId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100050, value = "Resource [%s] removed.")
    void resourceRemoved(String resourceId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100051, value = "Resource [%s] being transferred from persona [%s] to persona [%s].")
    void resourceTransferring(String resourceId, String oldPersonaId, String newPersonaId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100052, value = "Resource [%s] being transferred to persona [%s]. The resource had no owner before.")
    void resourceTransferringNoOwner(String resourceId, String newPersonaId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100053, value = "Revoking all permissions on resource [%s] for persona [%s]")
    void revokingAllForPersona(String resourceId, String newPersonaId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100054, value = "Persona [%s] already has role [%s] on resource [%s]. Will return the existing " +
            "record instead of creating a new one.")
    void personaAlreadyHaveRoleOnResource(String personaId, String roleName, String resourceId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100055, value = "Role [%s] created.")
    void roleCreated(String roleName);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100056, value = "User [%s] had its name changed. New name: [%s], old name: [%s].")
    void settingUsersName(String userId, String newName, String oldName);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100057, value = "User [%s] had its email changed. New email: [%s], old email: [%s].")
    void settingUsersEmail(String userId, String newEmail, String oldEmail);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100058, value = "User [%s] is not yet known to Accounts. Creating.")
    void creatingUser(String userId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100059, value = "User [%s], name [%s], is not yet known to Accounts. Creating.")
    void creatingUserWithName(String userId, String name);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 100060, value = "Listing all users known to Accounts. This should not happen in production code, " +
            "only in test code.")
    void listingAllUsers();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100061, value = "Creating user settings for user [%s].")
    void creatingSettings(String userId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100062, value = "Storing user setting for user [%s] named [%s] with value [%s].")
    void storedSetting(String userId, String key, String value);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 100063, value = "Removed user setting for user [%s] named [%s].")
    void removedSetting(String userId, String key);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 100064, value = "Cassandra session acquired.")
    void cassandraSessionAcquired();

}
