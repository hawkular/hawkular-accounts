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
package org.hawkular.accounts.secretstore.api;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Juraci Paixão Kröhling
 */
public class Token extends BaseEntity {
    private String refreshToken;
    private String secret;
    private String principal;
    private Map<String, String> attributes = new HashMap<>();

    protected Token(String refreshToken, String principal) {
        super(UUID.randomUUID());
        this.refreshToken = refreshToken;
        this.principal = principal;
        generateSecret();
    }

    public Token(UUID id, String refreshToken, String principal) {
        super(id);
        this.principal = principal;
        if (null == refreshToken) {
            throw new IllegalStateException("Refresh token should be provided for a new API/Secret pair.");
        }
        this.refreshToken = refreshToken;
        generateSecret();
    }

    public Token(UUID id, ZonedDateTime createdAt, ZonedDateTime updatedAt,
                 String refreshToken, String secret, Map<String, String> attributes, String principal) {
        super(id, createdAt, updatedAt);
        this.refreshToken = refreshToken;
        this.secret = secret;
        this.principal = principal;
        setAttributes(attributes);
    }

    private void generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] secretBytes = new byte[16];
        random.nextBytes(secretBytes);

        StringBuilder sb = new StringBuilder();
        for (byte b : secretBytes) {
            sb.append(String.format("%02X", b).toLowerCase());
        }
        this.secret = sb.toString();
    }

    public String getSecret() {
        return secret;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public void setAttributes(Map<String, String> attributes) {
        if (null == attributes) {
            this.attributes = new HashMap<>();
            return;
        }
        this.attributes = new HashMap<>(attributes);
    }

    public void addAttribute(String key, String value) {
        this.attributes.put(key, value);
    }

    public String getAttribute(String key) {
        return this.attributes.get(key);
    }

    public String getPrincipal() {
        return principal;
    }
}
