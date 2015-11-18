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
package org.hawkular.accounts.secretstore.boundary;

import java.util.Base64;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.hawkular.accounts.common.UsernamePasswordConverter;
import org.hawkular.accounts.secretstore.api.Token;
import org.hawkular.accounts.secretstore.api.TokenService;
import org.hawkular.accounts.secretstore.entity.TokenCreateResponse;
import org.hawkular.accounts.secretstore.entity.TokenErrorResponse;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;

/**
 * @author Juraci Paixão Kröhling
 */
@Path("tokens")
@Stateless
@PermitAll
public class TokenEndpoint {
    @Resource
    SessionContext sessionContext;

    @Context
    HttpServletRequest request;

    @Context
    ServletContext servletContext;

    @Inject
    TokenService tokenService;

    @Inject
    UsernamePasswordConverter usernamePasswordConverter;
    /**
     * This endpoint is called when Keycloak redirects the logged in user to our application.
     * @return  a response with a TokenCreateResponse as entity.
     */
    @GET
    @Path("create")
    public Response createFromRedirect() {
        KeycloakPrincipal principal = (KeycloakPrincipal) sessionContext.getCallerPrincipal();
        RefreshableKeycloakSecurityContext kcSecurityContext = (RefreshableKeycloakSecurityContext)
                principal.getKeycloakSecurityContext();

        String refreshToken = kcSecurityContext.getRefreshToken();
        return create(refreshToken);
    }

    /**
     * This endpoint is called when a client makes a REST call with basic auth.
     * @return  a response with a TokenCreateResponse as entity
     */
    @POST
    @Path("create")
    public Response createFromBasicAuth() throws Exception {
        String userAuthorizationHeader = request.getHeader("Authorization");
        String[] authorizationHeaderParts = userAuthorizationHeader.trim().split("\\s+");
        if (authorizationHeaderParts.length != 2) {
            TokenErrorResponse errorResponse = new TokenErrorResponse("Invalid authorization details.");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
        }

        if (!authorizationHeaderParts[0].equalsIgnoreCase("Basic")) {
            TokenErrorResponse errorResponse = new TokenErrorResponse("Only 'Basic' authentication is supported.");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
        }

        String authorization = new String(Base64.getDecoder().decode(authorizationHeaderParts[1]));
        String[] parts = authorization.split(":");
        String username = parts[0];
        String password = parts[1];

        if (username == null || username.isEmpty()) {
            TokenErrorResponse errorResponse = new TokenErrorResponse("Username is not provided.");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
        }

        String refreshToken = usernamePasswordConverter.getOfflineToken(username, password);
        return create(refreshToken);
    }

    private Response create(String refreshToken) {
        Token token = new Token(null, refreshToken);
        String personaId = request.getHeader("Hawkular-Persona");
        if (null != personaId && !personaId.isEmpty()) {
            token.addAttribute("persona_id", personaId);
        }
        tokenService.create(token);
        return Response.ok(new TokenCreateResponse(token)).build();
    }
}
