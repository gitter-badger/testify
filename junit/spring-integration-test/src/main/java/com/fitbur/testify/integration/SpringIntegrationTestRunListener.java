/*
 * Copyright 2015 Sharmarke Aden.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import com.fitbur.testify.need.NeedDescriptor;
import static java.util.Collections.EMPTY_LIST;
import java.util.List;
import java.util.Map;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * A JUnit run listener that listens for test case execution life-cycle and
 * manages spring application context.
 *
 * @author saden
 */
public class SpringIntegrationTestRunListener extends RunListener {

    public final String name;
    public final Map<Class, TestContext> testClassContexts;
    public final Map<Class, ServiceLocator> applicationContexts;
    private final Map<Class, List<NeedDescriptor>> testNeedDescriptors;

    SpringIntegrationTestRunListener(String name,
            Map<Class, TestContext> testClassContexts,
            Map<Class, ServiceLocator> applicationContexts,
            Map<Class, List<NeedDescriptor>> testNeedDescriptors) {
        this.name = name;
        this.testClassContexts = testClassContexts;
        this.applicationContexts = applicationContexts;
        this.testNeedDescriptors = testNeedDescriptors;
    }

    @Override
    public void testStarted(Description description) throws Exception {
        Class<?> testClass = description.getTestClass();
        List<NeedDescriptor> needs = testNeedDescriptors.getOrDefault(testClass, EMPTY_LIST);
        needs.stream()
                .forEach(p -> p.getTestNeed().before(p.getContext()));
    }

    @Override
    public void testFinished(Description description) throws Exception {
        this.closeApplicationContext(description);
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        this.closeApplicationContext(failure.getDescription());
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        this.closeApplicationContext(failure.getDescription());
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        this.closeApplicationContext(description);
    }

    private void closeApplicationContext(Description description) {
        Class<?> testClass = description.getTestClass();
        List<NeedDescriptor> needs = testNeedDescriptors.getOrDefault(testClass, EMPTY_LIST);
        needs.stream()
                .forEach(p -> p.getTestNeed().after(p.getContext()));
        this.applicationContexts.computeIfPresent(testClass, (k, v) -> {
            v.destroy();

            return null;
        });
    }

}
