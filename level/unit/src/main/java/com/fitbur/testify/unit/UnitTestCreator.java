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
package com.fitbur.testify.unit;

import com.fitbur.testify.Fake;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestReifier;
import com.fitbur.testify.descriptor.DescriptorKey;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.descriptor.ParameterDescriptor;
import com.fitbur.testify.unit.injector.UnitIndexFakeInjector;
import com.fitbur.testify.unit.injector.UnitNameFakeInjector;
import com.fitbur.testify.unit.injector.UnitTypeFakeInjector;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import static java.util.stream.Collectors.toSet;

/**
 * Unit test creator which looks at the test descriptors, reifies the
 * descriptors and injects them into test class.
 *
 * @author saden
 */
public class UnitTestCreator {

    private final TestContext testContext;
    private final TestReifier testReifier;

    public UnitTestCreator(TestContext testContext, TestReifier testReifier) {
        this.testContext = testContext;
        this.testReifier = testReifier;
    }

    public void create() {
        Map<DescriptorKey, ParameterDescriptor> parameterDescriptors = testContext.getParamaterDescriptors();
        Map<DescriptorKey, FieldDescriptor> fieldDescriptors = testContext.getFieldDescriptors();
        Object[] arguments = new Object[parameterDescriptors.size()];
        Collection<FieldDescriptor> descriptors = fieldDescriptors.values();

        UnitIndexFakeInjector indexInjector = new UnitIndexFakeInjector(testContext, testReifier, arguments);
        UnitNameFakeInjector nameInjector = new UnitNameFakeInjector(testContext, testReifier, arguments);
        UnitTypeFakeInjector typeInjector = new UnitTypeFakeInjector(testContext, testReifier, arguments);

        Set<FieldDescriptor> fakeDescriptors = descriptors.parallelStream()
                .filter(p -> p.hasAnnotation(Fake.class))
                .collect(toSet());

        //process fields with a custom index first
        fakeDescriptors.parallelStream()
                .forEach(indexInjector::inject);

        //process fields with custom names second
        fakeDescriptors.parallelStream()
                .forEach(nameInjector::inject);

        //finally process fields based on their type
        fakeDescriptors.parallelStream()
                .forEach(typeInjector::inject);

        testReifier.reifyCut(testContext.getCutDescriptor(), arguments);
    }

}
