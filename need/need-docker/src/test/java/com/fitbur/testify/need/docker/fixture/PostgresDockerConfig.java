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
package com.fitbur.testify.need.docker.fixture;

import com.fitbur.testify.need.NeedInstance;
import java.net.URI;
import javax.sql.DataSource;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

/**
 * Test Config
 *
 * @author saden
 */
@Configuration
@ComponentScan
public class PostgresDockerConfig {

    @Bean
    DataSource dataSourceProvider(NeedInstance instance) {
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
    LocalSessionFactoryBean localSessionFactoryBeanProvider(DataSource dataSource) {
        LocalSessionFactoryBean bean = new LocalSessionFactoryBean();
        ClassPathResource hibernateCfg = new ClassPathResource("hibernate.cfg.xml");

        bean.setDataSource(dataSource);
        bean.setConfigLocation(hibernateCfg);
        bean.setPackagesToScan(PostgresDockerConfig.class.getPackage().getName());
        bean.setPhysicalNamingStrategy(new PhysicalNamingStrategyStandardImpl());
        return bean;
    }

}
