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
package com.fitbur.testify.integration.fixture;

import com.fitbur.testify.need.NeedInstance;
import com.github.dockerjava.api.command.InspectContainerResponse;
import java.net.URI;
import javax.persistence.AttributeConverter;
import javax.persistence.Entity;
import javax.sql.DataSource;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyComponentPathImpl;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.postgresql.ds.PGPoolingDataSource;
import org.reflections.Reflections;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Test Config
 *
 * @author saden
 */
@Configuration
@ComponentScan
public class PostgresDockerConfig {

    @Bean
    DataSource dataSourceProvider(NeedInstance<InspectContainerResponse> instance) {
        URI uri = instance.findFirstURI().get();
        PGPoolingDataSource source = new PGPoolingDataSource();
        source.setDataSourceName("A Data Source");
        source.setServerName(instance.getHost());
        source.setPortNumber(instance.findFirstPort().get());
        source.setDatabaseName("postgres");
        source.setUser("postgres");
        source.setPassword("mysecretpassword");

        return source;

    }

    @Bean
    FactoryBean<SessionFactory> sessionFactoryImplProvider(DataSource dataSource) {
        return new SmartFactoryBean<SessionFactory>() {
            @Override
            public boolean isPrototype() {
                return false;
            }

            @Override
            public boolean isEagerInit() {
                return true;
            }

            @Override
            public SessionFactory getObject() throws Exception {
                StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                        .loadProperties("hibernate.properties")
                        .applySetting(Environment.DATASOURCE, dataSource)
                        .build();

                Reflections reflections = new Reflections(PostgresDockerConfig.class.getPackage().getName());
                MetadataSources metadataSources = new MetadataSources(registry);

                MetadataBuilder metadataBuilder = metadataSources.getMetadataBuilder()
                        .applyPhysicalNamingStrategy(new PhysicalNamingStrategyStandardImpl())
                        .applyImplicitNamingStrategy(new ImplicitNamingStrategyComponentPathImpl());

                reflections.getSubTypesOf(AttributeConverter.class).parallelStream()
                        .forEach(metadataBuilder::applyAttributeConverter);

                reflections.getTypesAnnotatedWith(Entity.class)
                        .parallelStream()
                        .forEach(metadataSources::addAnnotatedClass);

                Metadata metadata = metadataBuilder.build();

                return metadata.buildSessionFactory();
            }

            @Override
            public Class<?> getObjectType() {
                return SessionFactory.class;
            }

            @Override
            public boolean isSingleton() {
                return true;
            }
        };

    }

    @Bean
    Session sessionFactoryProvider(SessionFactory sessionFactory) {
        return sessionFactory.openSession();
    }

}
