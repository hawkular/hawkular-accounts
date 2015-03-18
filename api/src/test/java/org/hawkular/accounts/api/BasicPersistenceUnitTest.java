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
package org.hawkular.accounts.api;

import org.hawkular.accounts.api.model.BaseEntity;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Juraci Paixão Kröhling
 */
public class BasicPersistenceUnitTest {
    private EntityManager entityManager;

    @Before
    public void setup() {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("backend-unittest-pu");
        this.entityManager = entityManagerFactory.createEntityManager();
    }

    @Test
    public void testIsAbleToPersist() {
        BaseEntityTest baseEntityTest = new BaseEntityTest();

        entityManager.getTransaction().begin();
        entityManager.persist(baseEntityTest);
        entityManager.getTransaction().commit();
    }

    @Test
    public void createdAtIsConsistent() {
        BaseEntityTest baseEntityTest = new BaseEntityTest();

        entityManager.getTransaction().begin();
        entityManager.persist(baseEntityTest);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        BaseEntityTest fromDatabase = entityManager.find(BaseEntityTest.class, baseEntityTest.getId());
        entityManager.getTransaction().commit();

        assertEquals("Timezone should be kept", fromDatabase.getCreatedAt(), baseEntityTest.getCreatedAt());
    }

    @Test
    public void updatedAtIsRefreshed() {
        BaseEntityTest baseEntityTest = new BaseEntityTest();

        entityManager.getTransaction().begin();
        entityManager.persist(baseEntityTest);
        entityManager.getTransaction().commit();
        ZonedDateTime updatedAt = baseEntityTest.getUpdatedAt();

        entityManager.getTransaction().begin();
        baseEntityTest.setName("different name");
        entityManager.persist(baseEntityTest);
        entityManager.getTransaction().commit();

        assertNotEquals("Updated at should have been changed", updatedAt, baseEntityTest.getUpdatedAt());
    }

    @Entity
    public class BaseEntityTest extends BaseEntity {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
