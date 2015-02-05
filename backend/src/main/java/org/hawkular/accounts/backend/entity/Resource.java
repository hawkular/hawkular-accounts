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
package org.hawkular.accounts.backend.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 *
 * @author Juraci Paixão Kröhling <juraci at kroehling.de>
 */
@Entity
public class Resource extends BaseEntity {

  @ManyToOne
  private Owner owner;

  protected Resource() { // JPA happy
  }

  public Resource(Owner owner) {
    this.owner = owner;
  }

  public Resource(String id, Owner owner) {
    super(id);
    this.owner = owner;
  }

  public Owner getOwner() {
    return owner;
  }
}
