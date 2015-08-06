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
/**
 * Entities related to the API. Even though the models also have JPA annotations, the JPA aspect of the models are
 * not part of the public API, so, consumers should not assume that those models are JPA entities.
 *
 * @author Juraci Paixão Kröhling
 */
@XmlJavaTypeAdapters({@XmlJavaTypeAdapter(type=ZonedDateTime.class,value=ZonedDateTimeXmlAdapter.class)})
package org.hawkular.accounts.sample.entity;

import java.time.ZonedDateTime;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import org.hawkular.accounts.jaxb.adapters.ZonedDateTimeXmlAdapter;