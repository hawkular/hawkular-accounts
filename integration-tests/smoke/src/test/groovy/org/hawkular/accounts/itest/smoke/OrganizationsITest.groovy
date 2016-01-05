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
package org.hawkular.accounts.itest.smoke

import com.icegreen.greenmail.junit.GreenMailRule
import com.icegreen.greenmail.util.ServerSetupTest
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.jboss.arquillian.junit.Arquillian
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import javax.mail.internet.MimeMessage

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue
import static org.junit.Assert.fail

/**
 * @author Juraci Paixão Kröhling
 */
@RunWith(Arquillian.class)
class OrganizationsITest extends BaseSmokeTest {
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);

    @Test
    void createOrganizationWithoutVisibility() {
        def orgName = UUID.randomUUID().toString()
        def response = doCreateOrganization(orgName)

        assertEquals(200, response.status)
        assertNotNull(response.data.id)
        assertEquals('By default, the org should have been private', 'PRIVATE', response.data.visibility)
        assertEquals('The company name should have been persisted', orgName, response.data.name)
    }

    @Test
    void createOrganizationWithApplyVisibility() {
        def orgName = UUID.randomUUID().toString()
        def response = doCreateApplyOrganization(orgName)

        assertEquals(200, response.status)
        assertNotNull(response.data.id)
        assertEquals('APPLY', response.data.visibility)
    }

    @Test
    void createOrganizationWithPrivateVisibility() {
        def orgName = UUID.randomUUID().toString()
        def response = doCreatePrivateOrganization(orgName)

        assertEquals(200, response.status)
        assertNotNull(response.data.id)
        assertEquals('PRIVATE', response.data.visibility)
    }

    @Test
    void jsmithAppliesForOrganization() {
        String username = 'jsmith'
        String password = 'password'

        String orgName = UUID.randomUUID().toString()
        def response = doCreateApplyOrganization(orgName)
        String organizationId = response.data.id

        RESTClient jsmithClient = getClientForUsernameAndPassword(username, password)
        response = jsmithClient.post(path: "/hawkular/accounts/organizationJoinRequests/${organizationId}")
        String joinRequestId = response.data.id

        assertEquals(200, response.status)
        assertNotNull(joinRequestId)
        assertEquals('PENDING', response.data.status)
        assertTrue(greenMail.waitForIncomingEmail(5000, 2));

        // we expect two messages: one to the user who sent the join request, and one to the admin
        // both have the same subject... if the subject ever changes, we need to change the test as well
        for (MimeMessage message : greenMail.receivedMessages) {
            assertTrue("Expected to send a join request notification, but got another message",
                    message.getSubject().startsWith("[hawkular] - Join request for"));
        }

        RESTClient personaClient = getJdoeClientForPersona(organizationId)

        response = personaClient.put(
                path: "/hawkular/accounts/organizationJoinRequests/${organizationId}",
                body: [decision: 'ACCEPT', joinRequestId: joinRequestId]
        )

        assertEquals(200, response.status)
        assertNotNull(joinRequestId)
        assertEquals('ACCEPTED', response.data.status)
        assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        MimeMessage message = greenMail.receivedMessages[2];
        String expectedSubject = "[hawkular] - You have been accepted to join";
        assertTrue("Should have received an approval email", message.getSubject().startsWith(expectedSubject))

        // now, just a sanity check:
        RESTClient jsmithPersonaClient = getClientForPersona(username, password, organizationId)
        response = jsmithPersonaClient.get(path: '/hawkular/accounts/personas/current')
        assertEquals(200, response.status)
        assertEquals('Organization should have been impersonated', organizationId, response.data.id)
    }

    @Test
    void jsmithGetsApprovedTwice() {
        String username = 'jsmith'
        String password = 'password'

        String orgName = UUID.randomUUID().toString()
        def response = doCreateApplyOrganization(orgName)
        String organizationId = response.data.id

        RESTClient jsmithClient = getClientForUsernameAndPassword(username, password)
        response = jsmithClient.post(path: "/hawkular/accounts/organizationJoinRequests/${organizationId}")
        String joinRequestId = response.data.id
        RESTClient personaClient = getJdoeClientForPersona(organizationId)

        response = personaClient.put(
                path: "/hawkular/accounts/organizationJoinRequests/${organizationId}",
                body: [decision: 'ACCEPT', joinRequestId: joinRequestId]
        )
        assertEquals(200, response.status)

        response = personaClient.put(
                path: "/hawkular/accounts/organizationJoinRequests/${organizationId}",
                body: [decision: 'ACCEPT', joinRequestId: joinRequestId]
        )
        assertEquals(202, response.status)

    }

    @Test
    void jsmithGetsRejectedTwice() {
        String username = 'jsmith'
        String password = 'password'

        String orgName = UUID.randomUUID().toString()
        def response = doCreateApplyOrganization(orgName)
        String organizationId = response.data.id

        RESTClient jsmithClient = getClientForUsernameAndPassword(username, password)
        response = jsmithClient.post(path: "/hawkular/accounts/organizationJoinRequests/${organizationId}")
        String joinRequestId = response.data.id
        RESTClient personaClient = getJdoeClientForPersona(organizationId)

        response = personaClient.put(
                path: "/hawkular/accounts/organizationJoinRequests/${organizationId}",
                body: [decision: 'REJECT', joinRequestId: joinRequestId]
        )
        assertEquals(200, response.status)

        response = personaClient.put(
                path: "/hawkular/accounts/organizationJoinRequests/${organizationId}",
                body: [decision: 'REJECT', joinRequestId: joinRequestId]
        )
        assertEquals(202, response.status)

    }

    @Test
    void jsmithGetsRejectedAndThenAccepted() {
        String username = 'jsmith'
        String password = 'password'

        String orgName = UUID.randomUUID().toString()
        def response = doCreateApplyOrganization(orgName)
        String organizationId = response.data.id

        RESTClient jsmithClient = getClientForUsernameAndPassword(username, password)
        response = jsmithClient.post(path: "/hawkular/accounts/organizationJoinRequests/${organizationId}")
        String joinRequestId = response.data.id
        RESTClient personaClient = getJdoeClientForPersona(organizationId)

        response = personaClient.put(
                path: "/hawkular/accounts/organizationJoinRequests/${organizationId}",
                body: [decision: 'REJECT', joinRequestId: joinRequestId]
        )
        assertEquals(200, response.status)

        try {
            personaClient.put(
                    path: "/hawkular/accounts/organizationJoinRequests/${organizationId}",
                    body: [decision: 'ACCEPT', joinRequestId: joinRequestId]
            )
        } catch (HttpResponseException exception) {
            assertEquals(409, exception.response.status)
        }
    }

    @Test
    void jsmithGetsAcceptedAndThenRejected() {
        String username = 'jsmith'
        String password = 'password'

        String orgName = UUID.randomUUID().toString()
        def response = doCreateApplyOrganization(orgName)
        String organizationId = response.data.id

        RESTClient jsmithClient = getClientForUsernameAndPassword(username, password)
        response = jsmithClient.post(path: "/hawkular/accounts/organizationJoinRequests/${organizationId}")
        String joinRequestId = response.data.id
        RESTClient personaClient = getJdoeClientForPersona(organizationId)

        response = personaClient.put(
                path: "/hawkular/accounts/organizationJoinRequests/${organizationId}",
                body: [decision: 'ACCEPT', joinRequestId: joinRequestId]
        )
        assertEquals(200, response.status)

        try {
            personaClient.put(
                    path: "/hawkular/accounts/organizationJoinRequests/${organizationId}",
                    body: [decision: 'REJECT', joinRequestId: joinRequestId]
            )
        } catch (HttpResponseException exception) {
            assertEquals(409, exception.response.status)
        }
    }

    @Test
    void jsmithGetsRejectedForOrganization() {
        String username = 'jsmith'
        String password = 'password'

        String orgName = UUID.randomUUID().toString()
        def response = doCreateApplyOrganization(orgName)
        String organizationId = response.data.id

        RESTClient jsmithClient = getClientForUsernameAndPassword(username, password)
        response = jsmithClient.post(path: "/hawkular/accounts/organizationJoinRequests/${organizationId}")
        String joinRequestId = response.data.id

        assertEquals(200, response.status)
        assertNotNull(joinRequestId)
        assertEquals('PENDING', response.data.status)

        RESTClient personaClient = getJdoeClientForPersona(organizationId)
        response = personaClient.put(
                path: "/hawkular/accounts/organizationJoinRequests/${organizationId}",
                body: [decision: 'REJECT', joinRequestId: joinRequestId]
        )

        assertEquals(200, response.status)
        assertNotNull(joinRequestId)
        assertEquals('REJECTED', response.data.status)

        assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        MimeMessage message = greenMail.receivedMessages[2];
        String expectedSubject = "[hawkular] - Join request declined";
        assertEquals("Should have received a rejection email", expectedSubject, message.getSubject())

        // now, just a sanity check:
        RESTClient jsmithPersonaClient = getClientForPersona(username, password, organizationId)
        try {
            jsmithPersonaClient.get(path: '/hawkular/accounts/personas/current')
        } catch (HttpResponseException exception) {
            assertEquals(500, exception.response.status)
        }
    }

    @Test
    void jsmithAppliesForPrivateOrganization() {
        String username = 'jsmith'
        String password = 'password'

        String orgName = UUID.randomUUID().toString()
        def response = doCreatePrivateOrganization(orgName)

        String organizationId = response.data.id
        RESTClient jsmithClient = getClientForUsernameAndPassword(username, password)
        try {
            jsmithClient.post(path: "/hawkular/accounts/organizationJoinRequests/${organizationId}")
        } catch (HttpResponseException exception) {
            assertEquals(403, exception.response.status)
        }
    }

    @Test
    void organizationCannotApproveForAnotherOrganization() {
        String username = 'jsmith'
        String password = 'password'

        String orgName = UUID.randomUUID().toString()
        String orgName2 = UUID.randomUUID().toString()
        def response = doCreateApplyOrganization(orgName)
        def response2 = doCreateApplyOrganization(orgName2)

        String organizationId = response.data.id
        String organizationId2 = response2.data.id

        RESTClient jsmithClient = getClientForUsernameAndPassword(username, password)
        response = jsmithClient.post(path: "/hawkular/accounts/organizationJoinRequests/${organizationId}")
        String joinRequestId = response.data.id

        assertEquals(200, response.status)
        assertNotNull(joinRequestId)
        assertEquals('PENDING', response.data.status)

        // we have "organization 2" trying to approve a join request for "organization 1"
        RESTClient personaClient = getJdoeClientForPersona(organizationId2)
        try {
            personaClient.put(
                    path: "/hawkular/accounts/organizationJoinRequests/${organizationId}",
                    body: [decision: 'ACCEPT', joinRequestId: joinRequestId]
            )
            fail('Should have thrown an exception.')
        } catch (HttpResponseException exception) {
            assertEquals(403, exception.response.status)
        }
    }

    @Test
    void cannotSelfApproveToJoinOrganization() {
        String username = 'jsmith'
        String password = 'password'

        String orgName = UUID.randomUUID().toString()
        def response = doCreateApplyOrganization(orgName)

        String organizationId = response.data.id

        RESTClient jsmithClient = getClientForUsernameAndPassword(username, password)
        response = jsmithClient.post(path: "/hawkular/accounts/organizationJoinRequests/${organizationId}")
        String joinRequestId = response.data.id

        assertEquals(200, response.status)
        assertNotNull(joinRequestId)
        assertEquals('PENDING', response.data.status)

        // we have "organization 2" trying to approve a join request for "organization 1"
        try {
            jsmithClient.put(
                    path: "/hawkular/accounts/organizationJoinRequests/${organizationId}",
                    body: [decision: 'ACCEPT', joinRequestId: joinRequestId]
            )
            fail("Should have thrown an exception.")
        } catch (HttpResponseException exception) {
            assertEquals(403, exception.response.status)
        }
    }

    @Test
    void badDecision() {
        String username = 'jsmith'
        String password = 'password'

        String orgName = UUID.randomUUID().toString()
        def response = doCreateApplyOrganization(orgName)
        String organizationId = response.data.id

        RESTClient jsmithClient = getClientForUsernameAndPassword(username, password)
        response = jsmithClient.post(path: "/hawkular/accounts/organizationJoinRequests/${organizationId}")
        String joinRequestId = response.data.id

        assertEquals(200, response.status)
        assertNotNull(joinRequestId)
        assertEquals('PENDING', response.data.status)

        RESTClient personaClient = getJdoeClientForPersona(organizationId)

        try {
            personaClient.put(
                    path: "/hawkular/accounts/organizationJoinRequests/${organizationId}",
                    body: [decision: 'BAD_DECISION', joinRequestId: joinRequestId]
            )
            fail("Should have thrown an exception.")
        } catch (HttpResponseException exception) {
            assertEquals(400, exception.response.status)
            assertTrue("Should have failed due to bad decision.",
                    exception.response.data.message.startsWith("Invalid decision"))
        }
    }

}
