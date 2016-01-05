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
package com.fitbur.testify.integration;

import com.fitbur.testify.TestContext;
import com.fitbur.testify.di.ServiceLocator;
import com.fitbur.testify.di.spring.SpringServiceLocator;
import com.fitbur.testify.need.Need;
import com.fitbur.testify.need.NeedDescriptor;
import java.util.Optional;
import static java.util.Optional.ofNullable;

/**
 * Spring need descriptor.
 *
 * @author saden
 */
public class SpringIntegrationNeedDescriptor implements NeedDescriptor {

    private final Need need;
    private final TestContext testContext;
    private final SpringServiceLocator serviceLocator;

    public SpringIntegrationNeedDescriptor(Need need,
            TestContext testContext,
            SpringServiceLocator serviceLocator) {
        this.need = need;
        this.testContext = testContext;
        this.serviceLocator = serviceLocator;
    }

    @Override
    public Need getNeed() {
        return need;
    }

    @Override
    public Object getTestInstance() {
        return testContext.getTestInstance();
    }

    @Override
    public Class<?> getTestClass() {
        return testContext.getTestClass();
    }

    @Override
    public String getTestClassName() {
        return testContext.getTestClassName();
    }

    @Override
    public Optional<? extends ServiceLocator> getServiceLocator() {
        return ofNullable(serviceLocator);
    }

}
