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
package com.fitbur.testify.need.docker;

import com.fitbur.testify.Config;
import com.fitbur.testify.Module;
import com.fitbur.testify.Real;
import com.fitbur.testify.integration.SpringIntegrationTest;
import com.fitbur.testify.need.Need;
import com.fitbur.testify.need.docker.fixture.PostgresDockerConfig;
import com.fitbur.testify.need.docker.fixture.entity.UserEntity;
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
@Module(PostgresDockerConfig.class)
@Need(DockerNeedProvider.class)
@RunWith(SpringIntegrationTest.class)
@DockerContainer(value = "postgres")
public class DockerNeedProviderTest {

    @Real
    SessionFactory factory;

    @Config
    public void configure(DockerClientConfig.DockerClientConfigBuilder builder) {
        assertThat(builder).isNotNull();
    }

    @Test
    public void test() {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            UserEntity entity = new UserEntity(null, "saden", "test", "test");
            Serializable id = session.save(entity);
            tx.commit();
            assertThat(id).isNotNull();

            entity = session.get(UserEntity.class, id);
            assertThat(entity).isNotNull();
            javax.transaction.SystemException s;
        }
    }

    @Test
    public void test2() {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            UserEntity entity = new UserEntity(null, "saden", "test", "test");
            Serializable id = session.save(entity);
            tx.commit();
            assertThat(id).isNotNull();

            entity = session.get(UserEntity.class, id);
            assertThat(entity).isNotNull();
        }
    }
}
