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
package org.hawkular.accounts.undertow.filter;

import java.io.StringReader;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.AnnotationLiteral;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.hawkular.accounts.common.AuthServerRequestExecutor;
import org.hawkular.accounts.common.AuthServerUrl;
import org.hawkular.accounts.common.RealmName;
import org.hawkular.accounts.secretstore.api.Token;
import org.hawkular.accounts.secretstore.api.TokenService;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;

/**
 * @author Juraci Paixão Kröhling
 */
public class AgentHttpHandler implements HttpHandler {
    private static final Pattern UUID_PATTERN =
            Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
    private final MsgLogger logger = MsgLogger.LOGGER;

    private HttpHandler next;
    private String baseUrl;
    private String realm;

    public AgentHttpHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        if (httpServerExchange.isInIoThread()) {
            httpServerExchange.dispatch(this);
            return;
        }

        // steps:
        // - check and get client/secret for the secret-store
        // - check if the request contains an agent credential
        // - check if the credential exists and is valid
        // - lookup the token for the credential
        // - convert the refresh token into an access token
        // - set the Authorization header for Keycloak

        HeaderValues authorizationHeaders = httpServerExchange.getRequestHeaders().get("Authorization");
        if (authorizationHeaders == null || authorizationHeaders.size() < 1) {
            // nothing to do, credentials not provided
            finish(httpServerExchange);
            return;
        }

        String authorizationHeader = authorizationHeaders.getFirst();
        String[] authorizationHeaderParts = authorizationHeader.trim().split("\\s+");

        if (authorizationHeaderParts.length != 2) {
            finish(httpServerExchange);
            return;
        }

        if (!authorizationHeaderParts[0].equalsIgnoreCase("Basic")) {
            finish(httpServerExchange);
            return;
        }

        String authorization = new String(Base64.getDecoder().decode(authorizationHeaderParts[1]));
        String[] parts = authorization.split(":");
        String keyAsString = parts[0];
        String secret = parts[1];

        if (keyAsString == null || keyAsString.isEmpty()) {
            // nothing to do, credentials not provided
            finish(httpServerExchange);
            return;
        }

        if (!UUID_PATTERN.matcher(keyAsString).matches()) {
            // not an UUID, can't be a token
            finish(httpServerExchange);
            return;
        }

        UUID key;
        try {
            key = UUID.fromString(keyAsString);
        } catch (Throwable t) {
            // not an UUID, can't be a token
            finish(httpServerExchange);
            return;
        }

        TokenService tokenService = getTokenService();
        Token token = tokenService.validate(key, secret);

        if (token == null) {
            httpServerExchange.setResponseCode(403);
            httpServerExchange.endExchange();
            return;
        }

        String bearerToken = getBearerToken(token);
        if (bearerToken == null) {
            httpServerExchange.setResponseCode(403);
            httpServerExchange.endExchange();
            return;
        }

        httpServerExchange.getRequestHeaders().remove("Authorization");
        httpServerExchange.getRequestHeaders().remove("Hawkular-Persona");
        httpServerExchange.getRequestHeaders().put(new HttpString("Authorization"), "Bearer " + bearerToken);
        httpServerExchange.getRequestHeaders().put(
                new HttpString("Hawkular-Persona"), token.getAttribute("Hawkular-Persona")
        );

        finish(httpServerExchange);
    }

    private void finish(HttpServerExchange httpServerExchange) throws Exception {
        next.handleRequest(httpServerExchange);
    }

    private String getBearerToken(Token token) throws Exception {
        String tokenUrl = getAuthServerUrl()
                + "/realms/"
                + URLEncoder.encode(getRealm(), "UTF-8")
                + "/protocol/openid-connect/token";

        String urlParameters = "scope=offline_access&grant_type=refresh_token&refresh_token=" +
                URLEncoder.encode(token.getRefreshToken(), "UTF-8");

        AuthServerRequestExecutor executor = getAuthServerRequestExecutor();
        String sResponse = executor.execute(tokenUrl, urlParameters, "POST");
        JsonReader jsonReader = Json.createReader(new StringReader(sResponse));
        JsonObject object = jsonReader.readObject();
        if (object.get("error") != null) {
            String error = object.getString("error");
            logger.errorResponseFromServer(error);
            return null;
        }

        String tokenType = object.getString("token_type");
        String bearerToken = object.getString("access_token");

        if (null == tokenType || tokenType.isEmpty() || !tokenType.equalsIgnoreCase("bearer")) {
            logger.invalidResponseFromServer();
            return null;
        }

        if (null == bearerToken|| bearerToken.isEmpty()) {
            logger.invalidBearerTokenFromServer();
            return null;
        }

        return bearerToken;
    }

    @SuppressWarnings("unchecked")
    private TokenService getTokenService() {
        Thread.currentThread().setContextClassLoader(AgentHttpHandler.class.getClassLoader());
        BeanManager bm = CDI.current().getBeanManager();
        Bean<TokenService> bean = (Bean<TokenService>) bm.getBeans(TokenService.class).iterator().next();
        CreationalContext<TokenService> ctx = bm.createCreationalContext(bean);
        return (TokenService) bm.getReference(bean, TokenService.class, ctx);
    }

    @SuppressWarnings("unchecked")
    private AuthServerRequestExecutor getAuthServerRequestExecutor() {
        Thread.currentThread().setContextClassLoader(AgentHttpHandler.class.getClassLoader());
        BeanManager bm = CDI.current().getBeanManager();
        Bean<AuthServerRequestExecutor> bean = (Bean<AuthServerRequestExecutor>)
                bm.getBeans(AuthServerRequestExecutor.class).iterator().next();
        CreationalContext<AuthServerRequestExecutor> ctx = bm.createCreationalContext(bean);
        return (AuthServerRequestExecutor) bm.getReference(bean, AuthServerRequestExecutor.class, ctx);
    }

    @SuppressWarnings("unchecked")
    private String getAuthServerUrl() {
        if (null == baseUrl) {
            Thread.currentThread().setContextClassLoader(AgentHttpHandler.class.getClassLoader());
            BeanManager bm = CDI.current().getBeanManager();
            Bean<String> bean = (Bean<String>)
                    bm.getBeans(String.class, new AnnotationLiteral<AuthServerUrl>() {}).iterator().next();
            CreationalContext<String> ctx = bm.createCreationalContext(bean);
            baseUrl = (String) bm.getReference(bean, String.class, ctx);
        }
        return baseUrl;
    }

    @SuppressWarnings("unchecked")
    private String getRealm() {
        if (null == realm) {
            Thread.currentThread().setContextClassLoader(AgentHttpHandler.class.getClassLoader());
            BeanManager bm = CDI.current().getBeanManager();
            Bean<String> bean = (Bean<String>)
                    bm.getBeans(String.class, new AnnotationLiteral<RealmName>() {}).iterator().next();
            CreationalContext<String> ctx = bm.createCreationalContext(bean);
            realm = (String) bm.getReference(bean, String.class, ctx);
        }
        return realm;
    }

}
