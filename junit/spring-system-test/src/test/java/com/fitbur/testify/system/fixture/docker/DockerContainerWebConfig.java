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
package com.fitbur.testify.system.fixture.docker;

import com.fitbur.testify.need.NeedInstance;
import com.fitbur.testify.system.fixture.common.CommonConfig;
import com.fitbur.testify.system.fixture.common.SessionFactoryFactoryBean;
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
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 *
 * @author saden
 */
@Configuration
@EnableWebMvc
@Import(CommonConfig.class)
@ComponentScan
public class DockerContainerWebConfig {

    @Bean
    DataSource dataSourceProvider(NeedInstance<InspectContainerResponse> instance) {
        URI uri = instance.findFirstURI().get();
        PGSimpleDataSource source = new PGSimpleDataSource();
        source.setServerName(instance.getHost());
        source.setPortNumber(instance.findFirstPort().get());
        source.setDatabaseName("postgres");
        source.setUser("postgres");
        source.setPassword("mysecretpassword");

        return source;

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
