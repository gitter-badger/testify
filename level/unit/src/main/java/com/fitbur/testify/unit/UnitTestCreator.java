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

import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestReifier;
import com.fitbur.testify.descriptor.DescriptorKey;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.descriptor.ParameterDescriptor;
import com.fitbur.testify.unit.injector.IndexMockInjector;
import com.fitbur.testify.unit.injector.NameMockInjector;
import com.fitbur.testify.unit.injector.TypeMockInjector;
import java.util.Collection;
import java.util.Map;

/**
 * Unit test creator which looks at the test descriptors, reifies the
 * descriptors and injects them into test class.
 *
 * @author saden
 */
public class UnitTestCreator {

    private final TestContext context;
    private final TestReifier testReifier;

    public UnitTestCreator(TestContext context, TestReifier testReifier) {
        this.context = context;
        this.testReifier = testReifier;
    }

    public void create() {
        Map<DescriptorKey, ParameterDescriptor> parameterDescriptors = context.getParamaterDescriptors();
        Map<DescriptorKey, FieldDescriptor> fieldDescriptors = context.getFieldDescriptors();
        Object[] arguments = new Object[parameterDescriptors.size()];
        Collection<FieldDescriptor> descriptors = fieldDescriptors.values();

        //process fields with a custom index first
        descriptors.parallelStream()
                .filter(p -> p.getMock().isPresent())
                .filter(p -> p.getMock().get().index() != -1)
                .map(p -> new IndexMockInjector(context, testReifier, p, arguments))
                .forEach(IndexMockInjector::inject);

        //process fields with custom names second
        descriptors.parallelStream()
                .filter(p -> p.getMock().isPresent())
                .filter(p -> !p.getMock().get().name().isEmpty())
                .map(p -> new NameMockInjector(context, testReifier, p, arguments))
                .forEach(NameMockInjector::inject);

        //finally try to do type based injection
        descriptors.parallelStream()
                .filter(p -> p.getMock().isPresent())
                .filter(p -> p.getMock().get().index() == -1
                        && p.getMock().get().name().isEmpty()
                )
                .map(p -> new TypeMockInjector(context, testReifier, p, arguments))
                .forEach(TypeMockInjector::inject);

        testReifier.reifyCut(context.getCutDescriptor(), arguments);
    }

}
