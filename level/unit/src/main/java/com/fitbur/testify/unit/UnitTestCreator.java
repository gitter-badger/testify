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

        //process fields with a custom index first
        descriptors.parallelStream()
                .filter(p -> p.getAnnotation(Fake.class).isPresent())
                .filter(p -> p.getAnnotation(Fake.class).get().index() != -1)
                .map(p -> new UnitIndexFakeInjector(testContext, testReifier, p, arguments))
                .forEach(UnitIndexFakeInjector::inject);

        //process fields with custom names second
        descriptors.parallelStream()
                .filter(p -> p.getAnnotation(Fake.class).isPresent())
                .filter(p -> !p.getAnnotation(Fake.class).get().name().isEmpty())
                .map(p -> new UnitNameFakeInjector(testContext, testReifier, p, arguments))
                .forEach(UnitNameFakeInjector::inject);

        //finally try to do type based injection
        descriptors.parallelStream()
                .filter(p -> p.getAnnotation(Fake.class).isPresent())
                .filter(p -> p.getAnnotation(Fake.class).get().index() == -1
                        && p.getAnnotation(Fake.class).get().name().isEmpty()
                )
                .map(p -> new UnitTypeFakeInjector(testContext, testReifier, p, arguments))
                .forEach(UnitTypeFakeInjector::inject);

        testReifier.reifyCut(testContext.getCutDescriptor(), arguments);
    }

}
