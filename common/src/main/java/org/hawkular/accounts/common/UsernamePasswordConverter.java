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
package org.hawkular.accounts.common;

import java.io.StringReader;
import java.net.URLEncoder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * Converts an username/password into a token.
 *
 * @author Juraci Paixão Kröhling
 */
@ApplicationScoped
public class UsernamePasswordConverter {
    @Inject @AuthServerUrl
    private String baseUrl;

    @Inject @RealmName
    private String realm;

    @Inject
    AuthServerRequestExecutor executor;

    public String getAccessToken(String username, String password) throws Exception {
        JsonObject response = getResponse(username, password);
        return response.getString("access_token");
    }

    public String getRefreshToken(String username, String password) throws Exception {
        JsonObject response = getResponse(username, password);
        return response.getString("refresh_token");
    }

    public String getOfflineToken(String username, String password) throws Exception {
        if (username == null || username.isEmpty()) {
            throw new UsernamePasswordConversionException("Username is not provided.");
        }

        String tokenUrl = baseUrl + "/realms/" + URLEncoder.encode(realm, "UTF-8") + "/protocol/openid-connect/token";
        String urlParameters = "grant_type=password&username=" + URLEncoder.encode(username, "UTF-8");
        urlParameters += "&password=" + URLEncoder.encode(password, "UTF-8");
        urlParameters += "&scope=offline_access";

        String sResponse = executor.execute(tokenUrl, urlParameters, "POST");
        JsonReader jsonReader = Json.createReader(new StringReader(sResponse));
        JsonObject object = jsonReader.readObject();
        if (object.get("error") != null) {
            String error = object.getString("error");
            throw new UsernamePasswordConversionException("Error from Keycloak server: " + error);
        }

        return object.getString("refresh_token");
    }

    private JsonObject getResponse(String username, String password) throws Exception {
        String sResponse = getTokenResponseForUsernamePassword(username, password);
        JsonReader jsonReader = Json.createReader(new StringReader(sResponse));
        JsonObject object = jsonReader.readObject();
        if (object.get("error") != null) {
            String error = object.getString("error");
            throw new UsernamePasswordConversionException("Error from Keycloak server: " + error);
        }

        return object;
    }

    private String getTokenResponseForUsernamePassword(String username, String password) throws Exception {
        if (username == null || username.isEmpty()) {
            throw new UsernamePasswordConversionException("Username is not provided.");
        }

        String tokenUrl = baseUrl + "/realms/" + URLEncoder.encode(realm, "UTF-8") + "/protocol/openid-connect/token";
        String urlParameters = "grant_type=password&username=" + URLEncoder.encode(username, "UTF-8");
        urlParameters += "&password=" + URLEncoder.encode(password, "UTF-8");

        return executor.execute(tokenUrl, urlParameters, "POST");
    }
}
