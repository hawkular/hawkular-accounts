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
package org.hawkular.accounts.events.boundary;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.TopicSession;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hawkular.accounts.events.control.MsgLogger;
import org.hawkular.accounts.events.entity.EventCreateRequest;

/**
 * @author Juraci Paixão Kröhling
 */
@Path("/events")
@RequestScoped
public class EventsEndpoint {
    private final MsgLogger logger = MsgLogger.LOGGER;

    @Inject
    TopicSession topicSession;

    @Inject
    MessageProducer messageProducer;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAsJson(@Valid EventCreateRequest request) throws JMSException {
        return create(request);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createAsFormParameters(@Valid @BeanParam EventCreateRequest request) throws JMSException {
        return create(request);
    }

    @POST
    public Response create(EventCreateRequest request) throws JMSException {
        logger.eventReceived();
        Message message = topicSession.createMessage();
        message.setStringProperty("action", request.getAction());
        message.setStringProperty("userId", request.getUserId());
        message.setStringProperty("eventId", request.getEventId());
        messageProducer.send(message);
        messageProducer.close();
        logger.eventPublished();

        return Response.noContent().build();
    }

}
