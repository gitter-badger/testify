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
package com.fitbur.testify.examples.junit.spring.need;

import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Greeting module java config.
 *
 * @author saden
 */
@ComponentScan
@Configuration
@SuppressWarnings("ClassMayBeInterface")
public class GreetingConfig {

    @Bean
    DataSource dataSourceProvider() {
        EmbeddedDataSource dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName("GreetingApplication");
        dataSource.setDatabaseName("memory:GreetingApplication");
        dataSource.setCreateDatabase("create");

        return dataSource;
    }

}
