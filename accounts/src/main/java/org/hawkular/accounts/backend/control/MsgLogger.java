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
package org.hawkular.accounts.backend.control;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.ValidIdRange;

/**
 * JBoss Logging integration, with the possible messages that we have for this backend.
 *
 * @author Juraci Paixão Kröhling
 */
@org.jboss.logging.annotations.MessageLogger(projectCode = "HAWKACC")
@ValidIdRange(min = 110000, max = 119999)
public interface MsgLogger {
    MsgLogger LOGGER = Logger.getMessageLogger(MsgLogger.class, MsgLogger.class.getPackage().getName());

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 110000, value = "Started setting up Hawkular Accounts.")
    void startedSetupAccounts();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 110001, value = "Finished setting up Hawkular Accounts.")
    void finishedSetupAccounts();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 110004, value = "Invitation [%s] submitted. Token: [%s].")
    void invitationSubmitted(String invitationId, String token);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 110005, value = "Invitation [%s] is being reused by a different user [%s]. It was accepted by: [%s].")
    void invitationReused(String invitationId, String userTryingToUse, String acceptedBy);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 110006, value = "An exception has occurred while sending the message for invitation [%s]." +
            " Exception: [%s]")
    void invitationExceptionSendingMessage(String invitationId, String message);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 110007, value = "Join request made by a company. Not sending notifications.")
    void joinRequestToOrganization();

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 110008, value = "Join request made by an user with unknown email address. Not sending notifications.")
    void joinRequestUserEmailIsEmpty();

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 110009, value = "Failed to dispatch notification for accepted join request [%s]." +
            " Exception: ")
    void joinRequestFailToSendAcceptedNotification(String joinRequestId, @Cause Throwable cause);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 110010, value = "Failed to dispatch notification for rejected join request [%s]." +
            " Exception: ")
    void joinRequestFailToSendRejectedNotification(String joinRequestId, @Cause Throwable cause);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 110011, value = "Email address for organization's owner is not available. Not sending notifications.")
    void joinRequestOwnersEmailIsEmpty();

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 110012, value = "Failed to dispatch notification for join request [%s]. Exception: ")
    void joinRequestFailToSendNotification(String joinRequestId, @Cause Throwable cause);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110013, value = "No organization provided.")
    void missingOrganization();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110014, value = "Organization [%s] not found.")
    void organizationNotFound(String organizationId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110015, value = "Operation [%s] on [%s] cannot be performed by [%s].")
    void notAllowedToPerformOperationOnResource(String operationName, String resourceId, String personaId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110016, value = "Returning list of pending invitations for organization [%s].")
    void listPendingInvitations(String organizationId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110017, value = "Missing emails to send the invitation to.")
    void missingEmails();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110018, value = "Invitations queued for dispatching.")
    void invitationsSentToDispatch();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110019, value = "Invitation [%s] sent to [%s] to join the organization [%s].")
    void invitationSentToDispatch(String invitationId, String email, String organizationId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110020, value = "Missing token, cannot determine which invitation is being accepted.")
    void missingToken();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110021, value = "Could not find the invitation for the token [%s].")
    void invitationNotFound(String token);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110022, value = "The invitation [%s] is being accepted by the same user who created it: [%s].")
    void invitationAcceptedBySameUser(String token, String userId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110023, value = "The invitation [%s] has been accepted by [%s].")
    void invitationAccepted(String token, String userId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110024, value = "The persona [%s] is allowed to access [%d] organizations.")
    void numberOfOrganizationsForPersona(String token, int size);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110025, value = "The persona [%s] is allowed to access [%d] organizations.")
    void filteredOrganizationsWithReadPermission(String token, int size);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110026, value = "The persona [%s] can join [%d] organizations.")
    void filteredOrganizationsToJoin(String token, int size);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110027, value = "The organization [%s] is trying to create an organization, which is not supported.")
    void organizationTryingToCreateOrganization(String organizationId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110028, value = "A name is required in order to create an organization.")
    void missingOrganizationName();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110029, value = "An organization with the same name already exists.")
    void duplicateOrganizationName();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110030, value = "Missing visibility for new organization.")
    void missingVisibility();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110031, value = "Created organization [%s] with name [%s] and visibility [%s].")
    void createdOrganization(String id, String name, String visibility);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110032, value = "Cannot remove organization [%s] as it has sub organizations.")
    void organizationHasSuborganizations(String id);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110033, value = "Cannot remove organization [%s] as it has resources.")
    void organizationHasResources(String id);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110034, value = "Organization [%s] has been removed.")
    void organizationRemoved(String id);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110035, value = "Organization [%s] has been found.")
    void organizationFound(String id);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110036, value = "The given user ID is invalid (null).")
    void missingUser();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110037, value = "The user with the ID [%s] was not found.")
    void userNotFound(String id);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110038, value = "Organization [%s] has been transferred to [%s].")
    void organizationTransferred(String organizationId, String personaId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110039, value = "Received a request to add persona [%s] to organization [%s], but user already " +
            "belongs to it.")
    void alreadyMemberOfOrganization(String personaId, String organizationId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110040, value = "The user [%s] has tried to send a join request to organization [%s], but " +
            "organization is private and cannot accept invitations.")
    void privateOrganizationCannotAcceptJoinRequests(String personaId, String organizationId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110041, value = "Join request from persona [%s] to organization [%s] has been created.")
    void joinRequestCreated(String personaId, String organizationId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110042, value = "No decision provided.")
    void missingDecision();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110043, value = "Join request [%s] not found.")
    void joinRequestNotFound(String joinRequestId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110044, value = "Persona [%s] tried to accept a join request for [%s], but the join request was " +
            "made to join [%s]. Rejecting request.")
    void joinRequestBelongsToAnotherCompany(String personaId, String attemptedOrganization, String actualOrganization);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110045, value = "Persona [%s] tried to accept the join request [%s], but a decision about the join " +
            "request has already been made.")
    void joinRequestAlreadyDecidedUpon(String personaId, String joinRequestId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110046, value = "The decision [%s] is invalid.")
    void unknownDecision(String decision);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110047, value = "The decision for the join request [%s] has been made: [%s].")
    void decisionMade(String joinRequestId, String decision);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110048, value = "Listing only pending join requests.")
    void listOnlyPendingJoinRequests();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110049, value = "Listing all join requests.")
    void listAllJoinRequests();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110050, value = "Listing all join requests for persona [%s] resulted in [%d] records.")
    void listAllJoinRequestsForPersona(String personaId, int size);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110051, value = "Listing pending join requests for persona [%s] resulted in [%d] records.")
    void listPendingJoinRequestsForPersona(String personaId, int size);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110052, value = "Missing membership ID (null).")
    void missingMembershipId();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110053, value = "The membership [%s] could not be found.")
    void membershipNotFound(String membershipId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110054, value = "The membership [%s] found.")
    void membershipFound(String membershipId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110055, value = "Organization [%s] has [%d] memberships.")
    void numberOfMembershipsForOrganization(String organizationId, int size);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110056, value = "Missing role name (null).")
    void missingRole();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110057, value = "Role [%s] not found.")
    void roleNotFound(String roleName);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110058, value = "The membership [%s] has been updated to have role [%s].")
    void roleForMembershipChanged(String membershipId, String roleName);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110059, value = "Missing resource ID (null).")
    void missingResource();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110060, value = "Missing operation (null).")
    void missingOperation();

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110061, value = "Resource [%s] not found.")
    void resourceNotFound(String resourceId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110062, value = "Operation [%s] not found.")
    void operationNotFound(String operation);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110063, value = "Permission for [%s] on [%s] for persona [%s] resulted in [%b].")
    void permissionResponsePrepared(String operation, String resourceId, String personaId, boolean result);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110064, value = "User [%s] is allowed to use [%d] personas.")
    void userWithPersonas(String userId, int numOfPersonas);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110065, value = "User [%s] is allowed to use the persona [%s].")
    void userCanAccessPersona(String userId, String personaId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110066, value = "User [%s] is not allowed to use the persona [%d], or persona doesn't exist.")
    void userCannotAccessPersona(String userId, String personaId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110067, value = "Persona for the current request: [%s].")
    void personaForRequest(String personaId);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110068, value = "Persona [%s], on resource [%s], has [%d] roles.")
    void numberOfRolesForPersonaOnResource(String personaId, String resourceId, int numRoles);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = 110069, value = "Persona [%s] has [%d] settings.")
    void settingsForPersona(String personaId, int numSettings);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 110070, value = "An invitation event was created without event.")
    void invitationEventWithoutInvitation();

}
