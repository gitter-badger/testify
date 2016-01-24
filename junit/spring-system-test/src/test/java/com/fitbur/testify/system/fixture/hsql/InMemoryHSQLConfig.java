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
package com.fitbur.testify.system.fixture.hsql;

import com.fitbur.testify.need.NeedInstance;
import com.fitbur.testify.system.fixture.common.CommonConfig;
import com.fitbur.testify.system.fixture.common.SessionFactoryFactoryBean;
import javax.sql.DataSource;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Spring based java config for testify database need.
 *
 * @author saden
 */
@Configuration
@EnableWebMvc
@ComponentScan
@Import(CommonConfig.class)
public class InMemoryHSQLConfig {

    @Bean
    FactoryBean<SessionFactory> sessionFactoryImplProvider(NeedInstance<DataSource> instance) {
        return new SessionFactoryFactoryBean(instance.getInstance());
    }
}
