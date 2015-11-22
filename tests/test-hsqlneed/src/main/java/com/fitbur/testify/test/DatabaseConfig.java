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
package com.fitbur.testify.test;

import javax.sql.DataSource;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

/**
 * Spring based java config for testify database need.
 *
 * @author saden
 */
@Lazy
@Configuration
@ComponentScan(basePackageClasses = DatabaseConfig.class)
public class DatabaseConfig {

//    @Bean
//    public DataSource configureDataSource(DataSource dataSource) {
//        HikariConfig config = new HikariConfig();
//        config.setDataSource(dataSource);
//        config.addDataSourceProperty("cachePrepStmts", "true");
//        config.addDataSourceProperty("prepStmtCacheSize", "250");
//        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
//        config.addDataSourceProperty("useServerPrepStmts", "true");
//
//        return new HikariDataSource(config);
//    }
    @Bean
    LocalSessionFactoryBean localSessionFactoryBeanProvider(DataSource dataSource) {
        LocalSessionFactoryBean bean = new LocalSessionFactoryBean();
        ClassPathResource hibernateCfg = new ClassPathResource("hibernate.cfg.xml");

        bean.setDataSource(dataSource);
        bean.setConfigLocation(hibernateCfg);
        bean.setPackagesToScan(DatabaseConfig.class.getPackage().getName());
        bean.setPhysicalNamingStrategy(new PhysicalNamingStrategyStandardImpl());
        return bean;
    }
}
