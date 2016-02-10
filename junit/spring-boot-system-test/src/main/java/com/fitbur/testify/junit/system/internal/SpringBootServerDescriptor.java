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
package com.fitbur.testify.junit.system.internal;

import com.fitbur.testify.App;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.server.ServerDescriptor;
import java.util.Set;
import org.springframework.web.SpringServletContainerInitializer;

/**
 * A Spring Boot system test server descriptor.
 *
 * @author saden
 */
public class SpringBootServerDescriptor implements ServerDescriptor {

    private final App app;
    private final TestContext testContext;
    private final SpringServletContainerInitializer initializer;
    private final Set<Class<?>> handles;

    SpringBootServerDescriptor(App app,
            TestContext testContext,
            SpringServletContainerInitializer initializer,
            Set<Class<?>> handles) {
        this.app = app;
        this.testContext = testContext;
        this.initializer = initializer;
        this.handles = handles;

    }

    @Override
    public App getApp() {
        return app;
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
    public Set<Class<?>> getHandlesType() {
        return handles;
    }

    @Override
    public SpringServletContainerInitializer getServletContainerInitializer() {
        return initializer;
    }

}
