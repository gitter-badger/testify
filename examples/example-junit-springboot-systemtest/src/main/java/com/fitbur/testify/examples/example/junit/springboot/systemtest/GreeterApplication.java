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
package com.fitbur.testify.examples.example.junit.springboot.systemtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot Greeter Application.
 *
 * @author saden
 */
@SpringBootApplication
public class GreeterApplication {

    private final SpringApplication springApplication;

    public GreeterApplication(SpringApplication springApplication) {
        this.springApplication = springApplication;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication springApplication = new SpringApplication(GreeterApplication.class);
        new GreeterApplication(springApplication).run(args);

    }

    public void run(String[] args) {
        springApplication.run(args);
    }

}
