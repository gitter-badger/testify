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
package com.fitbur.testify.integration.injector;

import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestInjector;
import com.fitbur.testify.TestReifier;
import com.fitbur.testify.descriptor.DescriptorKey;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.descriptor.ParameterDescriptor;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * An integration test injector implementation that injects fields annotated
 * with {@link com.fitbur.testify.Real} or {@link javax.inject.Inject} that
 * performs name or type based detection on the class under test constructor and
 * injects services found in the dependency injection framework.
 *
 * @author saden
 */
public class IntegrationRealServiceInjector implements TestInjector {

    private final TestContext context;
    private final TestReifier testReifier;
    private final FieldDescriptor fieldDescriptor;
    private final Object[] arguments;

    public IntegrationRealServiceInjector(TestContext context,
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
            ParameterDescriptor descriptor = parameterDescriptors.get(descriptorKey);
            Integer index = descriptor.getIndex();

            Object instance = testReifier.reifyField(fieldDescriptor, descriptor);
            arguments[index] = instance;
        } else {
            //otherwise find the right parameter based on the type of the field
            Collection<ParameterDescriptor> descriptors = parameterDescriptors.values();
            for (ParameterDescriptor paramDescriptor : descriptors) {
                Parameter parameter = paramDescriptor.getParameter();
                Type parameterType = parameter.getParameterizedType();
                Integer index = paramDescriptor.getIndex();

                if (arguments[index] != null) {
                    continue;
                }

                if (parameterType.equals(fieldType)) {
                    Object instance = testReifier.reifyField(fieldDescriptor, paramDescriptor);
                    arguments[index] = instance;
                    break;
                }

            }
        }

    }

}
