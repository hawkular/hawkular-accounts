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
package org.hawkular.accounts.requestparameterauthfilter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Deque;
import java.util.Map;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

/**
 * @author Juraci Paixão Kröhling
 */
public class RequestParameterAuthHttpHandler implements HttpHandler {
    MsgLogger logger = MsgLogger.LOGGER;
    public static final String USERNAME = "u";
    public static final String PASSWORD = "p";

    private HttpHandler next;

    public RequestParameterAuthHttpHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        if (httpServerExchange.isInIoThread()) {
            httpServerExchange.dispatch(this);
            return;
        }

        Map<String, Deque<String>> requestParameters = httpServerExchange.getQueryParameters();
        if (null == requestParameters) {
            logger.skip();
            next.handleRequest(httpServerExchange);
            return;
        }

        // ideally, we would make these options configurable, but for now, it should suffice
        if (requestParameters.containsKey(USERNAME) && requestParameters.containsKey(PASSWORD)) {
            logger.converting();
            String username = requestParameters.get(USERNAME).getFirst();
            String password = requestParameters.get(PASSWORD).getFirst();
            String concat = username + ":" + password;
            String encoded = Base64.getEncoder().encodeToString(concat.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + encoded;
            httpServerExchange.getRequestHeaders().add(HttpString.tryFromString("Authorization"), authHeader);
            httpServerExchange.getQueryParameters().remove(USERNAME);
            httpServerExchange.getQueryParameters().remove(PASSWORD);
        }

        next.handleRequest(httpServerExchange);
    }
}
