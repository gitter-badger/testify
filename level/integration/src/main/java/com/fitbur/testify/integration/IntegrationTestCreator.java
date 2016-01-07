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

import com.fitbur.testify.Fake;
import com.fitbur.testify.Module;
import com.fitbur.testify.Scan;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestReifier;
import com.fitbur.testify.descriptor.DescriptorKey;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.descriptor.ParameterDescriptor;
import com.fitbur.testify.di.ServiceLocator;
import com.fitbur.testify.integration.injector.IntegrationIndexFakeInjector;
import com.fitbur.testify.integration.injector.IntegrationNameFakeInjector;
import com.fitbur.testify.integration.injector.IntegrationRealServiceInjector;
import com.fitbur.testify.integration.injector.IntegrationTypeFakeInjector;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;

/**
 * A spring integration test creator which looks at the test descriptors,
 * reifies the descriptors and injects the test class with beans from spring
 * application context or mocks.
 *
 * @author saden
 */
public class IntegrationTestCreator {

    private final TestContext context;
    private final TestReifier testReifier;
    private final ServiceLocator locator;

    public IntegrationTestCreator(TestContext testContext, TestReifier testReifier, ServiceLocator locator) {
        this.context = testContext;
        this.testReifier = testReifier;
        this.locator = locator;
    }

    public void cut() {
        Map<DescriptorKey, ParameterDescriptor> parameterDescriptors = context.getParamaterDescriptors();
        Map<DescriptorKey, FieldDescriptor> fieldDescriptors = context.getFieldDescriptors();
        Object[] arguments = new Object[parameterDescriptors.size()];
        Collection<FieldDescriptor> descriptors = fieldDescriptors.values();

        Set<FieldDescriptor> fakeDescriptors = descriptors.parallelStream()
                .filter(p -> p.getAnnotation(Fake.class).isPresent())
                .collect(toSet());

        Class<?> testClass = context.getTestClass();

        of(testClass.getDeclaredAnnotationsByType(Module.class))
                .map(Module::value)
                .distinct()
                .forEachOrdered(locator::addModule);

        of(testClass.getDeclaredAnnotationsByType(Scan.class))
                .map(Scan::value)
                .distinct()
                .forEachOrdered(locator::scanPackage);

        IntegrationIndexFakeInjector indexInjector = new IntegrationIndexFakeInjector(context, testReifier, arguments);
        IntegrationNameFakeInjector nameInjector = new IntegrationNameFakeInjector(context, testReifier, arguments);
        IntegrationTypeFakeInjector typeInjector = new IntegrationTypeFakeInjector(context, testReifier, arguments);

        //process fields with a custom index first
        fakeDescriptors.parallelStream()
                .forEach(indexInjector::inject);

        //process fields with custom names second
        fakeDescriptors.parallelStream()
                .forEach(nameInjector::inject);

        //finally process fields based on their type
        fakeDescriptors.parallelStream()
                .forEach(typeInjector::inject);

        locator.reload();

        IntegrationRealServiceInjector realInjector = new IntegrationRealServiceInjector(context, locator, testReifier, arguments);
        descriptors.parallelStream()
                .forEach(realInjector::inject);

        testReifier.reifyCut(context.getCutDescriptor(), arguments);
    }

    public void real(Set<FieldDescriptor> fieldDescriptors) {
        Class<?> testClass = context.getTestClass();
        of(testClass.getDeclaredAnnotationsByType(Module.class))
                .map(Module::value)
                .distinct()
                .forEachOrdered(locator::addModule);

        locator.reload();

        testReifier.reifyTest(fieldDescriptors);
    }

}
