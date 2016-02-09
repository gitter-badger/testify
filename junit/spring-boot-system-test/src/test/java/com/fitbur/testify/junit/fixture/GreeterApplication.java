/*
 * Copyright 2016 Sharmarke Aden.
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
package com.fitbur.testify.junit.fixture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.web.SpringBootServletInitializer;

/**
 *
 * @author saden
 */
@SpringBootApplication
public class GreeterApplication extends SpringBootServletInitializer
        implements EmbeddedServletContainerCustomizer {

    @Override
    public SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//        return application.sources(GreeterApplication.class);
        return application;
    }

    public static void main(String[] args) throws Exception {
        new SpringApplication(GreeterApplication.class).run(args);
        SpringApplication.run(GreeterApplication.class, args);
    }

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        container.setPort(9000);
    }

}
