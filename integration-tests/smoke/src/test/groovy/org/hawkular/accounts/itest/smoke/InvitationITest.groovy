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
package org.hawkular.accounts.itest.smoke

import com.icegreen.greenmail.junit.GreenMailRule
import com.icegreen.greenmail.util.ServerSetupTest
import org.jboss.arquillian.junit.Arquillian
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import java.util.regex.Matcher
import java.util.regex.Pattern

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

/**
 * @author Juraci Paixão Kröhling
 */
@RunWith(Arquillian.class)
class InvitationITest extends BaseSmokeTest {
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);

    @Test
    void inviteUserToOrganization() {
        def response = client.post(
                path: '/hawkular/accounts/organizations',
                body: [name: UUID.randomUUID().toString()]
        )
        assertEquals(200, response.status)
        def organizationId = response.data.id

        response = client.post(
                path: '/hawkular/accounts/invitations',
                body: [organizationId: organizationId, emails: 'foo@foobar.com']
        )
        assertEquals(204, response.status)
        assertTrue(greenMail.waitForIncomingEmail(5000, 1)); // 5 seconds timeout to receive one message
        MimeMessage message = greenMail.receivedMessages[0];
        String body = ((MimeMultipart)message.content).getBodyPart(1).content

        Pattern pattern = Pattern.compile("[a-z]+:\\/\\/[^ \\n]*")
        Matcher matcher = pattern.matcher(body);
        assertTrue(matcher.find())

        String url = body.substring(matcher.start(), matcher.end())
        def token = url.substring(url.lastIndexOf('/')+1).trim()

        Pattern uuidPattern = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")
        assertTrue(uuidPattern.matcher(token).matches())
    }
}
