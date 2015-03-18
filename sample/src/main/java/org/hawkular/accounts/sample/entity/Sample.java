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
package org.hawkular.accounts.sample.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Juraci Paixão Kröhling
 */
@Entity
public class Sample {

    @Id
    private String id;

    private String name;

    private String ownerId;

    protected Sample() { // jpa happy
    }

    public Sample(String id, String ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerId() {
        return ownerId;
    }
}
