/*
 * Copyright 2015 Sharmarke Aden.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fitbur.testify.junit.system;

import com.fitbur.testify.App;
import com.fitbur.testify.Config;
import com.fitbur.testify.Real;
import com.fitbur.testify.junit.fixture.common.UserEntity;
import com.fitbur.testify.junit.fixture.docker.DockerContainerApplication;
import com.fitbur.testify.need.NeedContainer;
import com.github.dockerjava.core.DockerClientConfig;
import java.io.Serializable;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author saden
 */
@RunWith(SpringBootSystemTest.class)
@App(DockerContainerApplication.class)
@NeedContainer(value = "postgres", version = "9.4")
public class PostgresNeedContainerTest {

    @Real
    SessionFactory factory;

    @Config
    public void configure(DockerClientConfig.DockerClientConfigBuilder builder) {
        assertThat(builder).isNotNull();
    }

    @Test
    public void givenUserEntitySaveShouldPerisistEntityToPostgres() {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            UserEntity entity = new UserEntity(null, "saden", "test1", "test1");
            Serializable id = session.save(entity);
            tx.commit();
            assertThat(id).isNotNull();

            entity = session.get(UserEntity.class, id);
            assertThat(entity).isNotNull();
            javax.transaction.SystemException s;
        }
    }

    @Test
    public void givenAnotherUserEntitySaveShouldPerisistEntityToPostgres() {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            UserEntity entity = new UserEntity(null, "saden", "test2", "test2");
            Serializable id = session.save(entity);
            tx.commit();
            assertThat(id).isNotNull();

            entity = session.get(UserEntity.class, id);
            assertThat(entity).isNotNull();
        }
    }
}
