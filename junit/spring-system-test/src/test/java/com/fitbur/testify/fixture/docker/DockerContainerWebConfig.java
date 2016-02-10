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
package com.fitbur.testify.fixture.docker;

import com.fitbur.testify.fixture.common.DatabaseConfig;
import com.fitbur.testify.fixture.common.SessionFactoryFactoryBean;
import com.fitbur.testify.fixture.web.GreeterWebConfig;
import com.fitbur.testify.need.NeedInstance;
import com.github.dockerjava.api.command.InspectContainerResponse;
import java.net.URI;
import javax.sql.DataSource;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 *
 * @author saden
 */
@Configuration
@ComponentScan
@Import({GreeterWebConfig.class, DatabaseConfig.class})
public class DockerContainerWebConfig {

    @Bean
    DataSource dataSourceProvider(NeedInstance<InspectContainerResponse> instance) {
        URI uri = instance.findFirstURI().get();
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerName(instance.getHost());
        dataSource.setPortNumber(instance.findFirstPort().get());
        //Default postgres image database name, user and postword
        dataSource.setDatabaseName("postgres");
        dataSource.setUser("postgres");
        dataSource.setPassword("mysecretpassword");

        return dataSource;

    }

    @Bean
    FactoryBean<SessionFactory> sessionFactoryImplProvider(DataSource dataSource) {
        return new SessionFactoryFactoryBean(dataSource);
    }

    @Bean
    Session sessionFactoryProvider(SessionFactory sessionFactory) {
        return sessionFactory.openSession();
    }
}
