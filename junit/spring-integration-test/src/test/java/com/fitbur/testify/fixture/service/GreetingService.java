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
package com.fitbur.testify.fixture.service;

import com.fitbur.testify.fixture.service.collaborator.Hello;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author saden
 */
@Component
public class GreetingService {

    private final Hello hello;

    @Autowired
    GreetingService(Hello hello) {
        this.hello = hello;
    }

    public String greet() {
        return hello.greet();
    }

    public Hello getHello() {
        return hello;
    }

}
