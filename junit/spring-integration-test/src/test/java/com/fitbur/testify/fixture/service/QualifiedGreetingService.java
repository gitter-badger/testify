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

import com.fitbur.testify.fixture.service.collaborator.Greeting;
import com.fitbur.testify.fixture.service.collaborator.qualfied.annotation.JSR330Qualifier;
import com.fitbur.testify.fixture.service.collaborator.qualfied.annotation.SpringQualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author saden
 */
@Component
public class QualifiedGreetingService {

    private final Greeting jsr330Qualified;
    private final Greeting springQualified;

    @Autowired
    QualifiedGreetingService(@JSR330Qualifier Greeting jsr330Qualified, @SpringQualifier Greeting springQualified) {
        this.jsr330Qualified = jsr330Qualified;
        this.springQualified = springQualified;
    }

    public String greet() {
        return jsr330Qualified.greet() + springQualified.greet();
    }

    public Greeting getJSRQualified() {
        return jsr330Qualified;
    }

    public Greeting getSpringQualified() {
        return springQualified;
    }

}
