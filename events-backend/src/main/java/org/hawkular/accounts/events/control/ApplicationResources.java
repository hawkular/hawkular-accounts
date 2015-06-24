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
package org.hawkular.accounts.events.control;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;

/**
 * @author Juraci Paixão Kröhling
 */
@ApplicationScoped
public class ApplicationResources {

    @Resource(name = "java:/HawkularBusConnectionFactory")
    private TopicConnectionFactory busConnectionFactory;

    @Produces @Resource(name = "java:/topic/HawkularAccountsEvents")
    private Topic accountsEventsTopic;

    @Produces
    public TopicConnection createAccountsEventsConnection() throws JMSException {
        return busConnectionFactory.createTopicConnection();
    }

    @Produces
    public MessageProducer createAccountsEventsMessageProducer(TopicSession session) throws JMSException {
        return session.createPublisher(accountsEventsTopic);
    }

    @Produces
    public TopicSession createAccountsEventsSession(TopicConnection connection) throws JMSException {
        return connection.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
    }

    public void closeAccountsEventsConnection(@Disposes TopicConnection connection) throws JMSException {
        connection.close();
    }

    public void closeAccountsEventsSession(@Disposes TopicSession session) throws JMSException {
        session.close();
    }
}
