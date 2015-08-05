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
package org.hawkular.accounts.sample.websocket.backend;

import java.io.IOException;
import java.io.StringReader;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.CloseReason;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.hawkular.accounts.websocket.Authenticator;
import org.hawkular.accounts.websocket.WebsocketAuthenticationException;

@ServerEndpoint(value = "/socket")
public class Socket {
    @Inject
    Authenticator authenticator;

    @OnMessage
    public String onMessage(String message, Session session) throws IOException {
        authenticate(message, session);
        JsonReader jsonReader = Json.createReader(new StringReader(message));
        JsonObject jsonMessage = jsonReader.readObject();
        return jsonMessage.getString("message");
    }

    private void authenticate(String message, Session session) throws IOException {
        try {
            authenticator.authenticateWithMessage(message, session);
        } catch (WebsocketAuthenticationException e) {
            session.close(new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, e.getLocalizedMessage()));
        }
    }
}
