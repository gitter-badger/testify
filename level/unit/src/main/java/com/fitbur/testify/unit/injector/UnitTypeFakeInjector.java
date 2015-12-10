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
package com.fitbur.testify.unit.injector;

import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestInjector;
import com.fitbur.testify.TestReifier;
import com.fitbur.testify.descriptor.DescriptorKey;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.descriptor.ParameterDescriptor;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * A unit test injector implementation that injects fields annotated with
 * {@link com.fitbur.testify.Mock} that does some smart name/type based
 * detection on the cut class * constructor.
 *
 * @author saden
 */
public class UnitTypeFakeInjector implements TestInjector {

    private final TestContext context;
    private final TestReifier testReifier;
    private final FieldDescriptor fieldDescriptor;
    private final Object[] arguments;

    public UnitTypeFakeInjector(TestContext context,
            TestReifier testReifier,
            FieldDescriptor fieldDescriptor,
            Object[] arguments) {
        this.context = context;
        this.testReifier = testReifier;
        this.fieldDescriptor = fieldDescriptor;
        this.arguments = arguments;
    }

    @Override
    public void inject() {
        Map<DescriptorKey, ParameterDescriptor> parameterDescriptors = context.getParamaterDescriptors();

        Type fieldType = fieldDescriptor.getGenericType();
        String fieldName = fieldDescriptor.getName();
        DescriptorKey descriptorKey = new DescriptorKey(fieldType, fieldName);

        //if there is a parameter descriptor that matches the field then lets use that
        if (parameterDescriptors.containsKey(descriptorKey)) {
            ParameterDescriptor paramDescriptor = parameterDescriptors.get(descriptorKey);
            Integer paramIndex = paramDescriptor.getIndex();
            arguments[paramIndex] = testReifier.reifyField(fieldDescriptor, paramDescriptor);
        } else {
            //otherwise find the right parameter based on the type of the field
            Collection<ParameterDescriptor> descriptors = parameterDescriptors.values();
            for (ParameterDescriptor paramDescriptor : descriptors) {
                Integer paramIndex = paramDescriptor.getIndex();

                if (arguments[paramIndex] != null) {
                    continue;
                }

                Type paramType = paramDescriptor.getGenericType();
                if (paramType.equals(fieldType)) {
                    arguments[paramIndex] = testReifier.reifyField(fieldDescriptor, paramDescriptor);
                    break;
                }

            }
        }

    }

}
