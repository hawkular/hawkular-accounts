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
package org.hawkular.accounts.websocket;

import static org.hawkular.accounts.websocket.internal.AuthenticationMode.MESSAGE;
import static org.hawkular.accounts.websocket.internal.AuthenticationMode.TOKEN;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.Session;

import org.hawkular.accounts.api.PersonaService;
import org.hawkular.accounts.api.UserService;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.common.TokenVerifier;
import org.hawkular.accounts.common.UsernamePasswordConverter;
import org.hawkular.accounts.websocket.internal.AuthenticationMode;
import org.hawkular.accounts.websocket.internal.CachedSession;

/**
 * Helper integration for Server Web Socket Endpoints. Each message coming to a Web Socket should be passed to this
 * authenticator first. If authentication data can be derived for the connection or from the message, processing
 * continues. Otherwise, the exception {@link WebsocketAuthenticationException} is thrown.
 * <p>
 * This authenticator includes a cache for sessions, so that if a session and message fulfills the
 * following conditions, the message is accepted and the session is understood as sufficiently authenticated:
 * <ul>
 * <li>The current message has no authentication data, but a previous one did have valid auth data</li>
 * <li>The current message has no persona in the authentication, or is the same as the original persona.</li>
 * <li>The expiration timestamp for the original token has not elapsed yet.</li>
 * </ul>
 *
 * @author Juraci Paixão Kröhling
 */
@ApplicationScoped
public class Authenticator {

    @Inject
    UsernamePasswordConverter usernamePasswordConverter;

    @Inject
    TokenVerifier tokenVerifier;

    @Inject
    PersonaService personaService;

    @Inject
    UserService userService;

    /**
     * Minimalistic cache for sessions.
     * TODO: convert this into a proper Cache.
     */
    private Map<String, CachedSession> cachedSessions = new HashMap<>();

    /**
     * Authenticates the user/persona that sent the message based on either the message itself or based on previous
     * messages (looked up via the session ID).
     * <p>
     * Sample messages:<br/>
     * With token - {"authentication": {"token": "abc123def"}, "mypayload": {"message":"hello world"}}<br/>
     * User/pass - {"authentication":
     * {"login": {"username": "jdoe", "password":"securepass"}}, "mypayload":{"message":"hello world"}}<br/>
     *
     * @param message    JSON message with an {@code authentication} object, which should include either a {@code token}
     *                   object or {@code username} and {@code password}.
     * @param session    the Web Socket session for this message.
     * @throws WebsocketAuthenticationException if authentication cannot be inferred from the message nor from the
     * session.
     */
    public void authenticateWithMessage(String message, Session session) throws WebsocketAuthenticationException {
        try (JsonReader jsonReader = Json.createReader(new StringReader(message))) {
            JsonObject jsonMessage = jsonReader.readObject();
            JsonObject jsonAuth = jsonMessage.getJsonObject("authentication");
            String personaId = null;
            if (jsonAuth != null) {
                personaId = jsonAuth.getString("persona");
            }

            authenticate(MESSAGE, personaId, session, jsonAuth, null, null, null);
        }
    }

    /**
     * Authenticates the user/persona that sent the message based on the token or based on previous messages (looked
     * up via the session ID).
     * @param token      the bearer token to be validated
     * @param session    the Web Socket session
     * @throws WebsocketAuthenticationException if authentication cannot be inferred from the token nor from the
     * session.
     */
    public void authenticateWithToken(String token, String personaId, Session session)
            throws WebsocketAuthenticationException {
        authenticate(TOKEN, personaId, session, null, token, null, null);
    }

    /**
     * Authenticates the user/persona that sent the message based on the credentials or based on previous messages
     * (looked up via the session ID).
     * @param username   the username
     * @param password   the password
     * @param session    the Web Socket session
     * @throws WebsocketAuthenticationException if authentication cannot be inferred from the credentials nor from the
     * session.
     */
    public void authenticateWithCredentials(String username, String password, String personaId, Session session)
            throws WebsocketAuthenticationException {
        authenticate(TOKEN, personaId, session, null, null, username, password);
    }

    private void authenticate(
            AuthenticationMode mode,
            String personaId,
            Session session,
            JsonObject jsonAuth,
            String token,
            String username,
            String password
    ) throws WebsocketAuthenticationException {
        // do we have this session on the cache?
        CachedSession cachedSession = cachedSessions.get(session.getId());

        if (isValid(cachedSession, personaId)) {
            // the session is still valid, so, just return
            return;
        }

        if (null == cachedSession || !isValid(cachedSession, personaId)) {
            try {
                switch (mode) {
                    case CREDENTIALS:
                        cachedSession = doAuthenticationWithToken(personaId, token);
                        break;
                    case MESSAGE:
                        cachedSession = doAuthenticationWithMessage(personaId, jsonAuth);
                        break;
                    case TOKEN:
                        cachedSession = doAuthenticationWithCredentials(personaId, username, password);
                        break;
                    default:
                        throw new WebsocketAuthenticationException("Could not determine the authentication mode " +
                                "(token, message, credentials).");
                }
            } catch (WebsocketAuthenticationException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // cached session is valid!
        if (null != cachedSession) {
            this.cachedSessions.putIfAbsent(session.getId(), cachedSession);
        } else {
            // not that I'm trying to be rude, but...
            throw new WebsocketAuthenticationException("No authentication data provided.");
        }
    }

    private CachedSession doAuthenticationWithMessage(String personaId, JsonObject jsonAuth) throws Exception {
        if (null == jsonAuth) {
            return null;
        }

        // now, we have either a "token" or a "login" object
        if (jsonAuth.containsKey("token")) {
            String authToken = jsonAuth.getString("token");
            return doAuthenticationWithToken(personaId, authToken);
        }

        // the only other possible option is credentials, so...
        JsonObject jsonLogin = jsonAuth.getJsonObject("login");
        if (null != jsonLogin) {
            String username = jsonLogin.getString("username");
            String password = jsonLogin.getString("password");
            return doAuthenticationWithCredentials(personaId, username, password);
        }

        return null;
    }

    private CachedSession doAuthenticationWithCredentials(String personaId, String username, String password) throws
            Exception {
        if (null == username || username.isEmpty()) {
            return null;
        }

        if (null == password || password.isEmpty()) {
            return null;
        }

        String token = usernamePasswordConverter.getAccessToken(username, password);
        return doAuthenticationWithToken(personaId, token);
    }

    private CachedSession doAuthenticationWithToken(String personaId, String authToken) throws Exception {
        if (null == authToken) {
            return null;
        }

        String accessToken = tokenVerifier.verify(authToken);
        JsonReader jsonReader = Json.createReader(new StringReader(accessToken));
        JsonObject accessTokenJson = jsonReader.readObject();

        if (accessToken.contains("error_description")) {
            String errorDescription = accessTokenJson.getString("error_description");
            String error = accessTokenJson.getString("error");
            throw new WebsocketAuthenticationException(
                    "Authentication server returned an error. Error: " + error +
                            ". Error description: " + errorDescription
            );
        }

        String userId = accessTokenJson.getString("sub");

        if (null == userId || userId.isEmpty()) {
            throw new IllegalStateException("Subject wasn't returned by the authentication server.");
        }

        long expirationTime = accessTokenJson.getInt("exp");
        expirationTime*= 1000;

        HawkularUser actualUser = userService.getOrCreateById(userId);

        Persona persona;
        if (null != personaId && !personaId.equals(userId)) {
            Persona personaToCheck = personaService.get(personaId);
            if (null == personaToCheck) {
                // persona was not found!
                throw new WebsocketAuthenticationException("Persona not found.");
            }

            if (personaService.isAllowedToImpersonate(actualUser, personaToCheck)) {
                persona = personaToCheck;
            } else {
                throw new WebsocketAuthenticationException("User is not allowed to impersonate this persona.");
            }
        } else {
            persona = actualUser;
        }

        return new CachedSession(accessToken, persona, expirationTime);
    }

    private boolean isValid(CachedSession cachedSession, String personaId) {
        if (null == cachedSession) {
            return false;
        }

        if (personaId != null && !cachedSession.getPersona().getId().equals(personaId)) {
            // session is for a different persona, force a new authentication
            return false;
        }

        return System.currentTimeMillis() < cachedSession.getExpiresAt();
    }
}
