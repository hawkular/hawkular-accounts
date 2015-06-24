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
package org.hawkular.accounts.events.entity;

import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;

/**
 * @author Juraci Paixão Kröhling
 */
public class EventCreateRequest {

    @FormParam("userId")
    @NotNull
    private String userId;

    @FormParam("eventId")
    @NotNull
    private String eventId;

    @FormParam("action")
    @NotNull
    private String action;

    public EventCreateRequest(String userId, String eventId, String action) {
        this.userId = userId;
        this.eventId = eventId;
        this.action = action;
    }

    public EventCreateRequest() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
