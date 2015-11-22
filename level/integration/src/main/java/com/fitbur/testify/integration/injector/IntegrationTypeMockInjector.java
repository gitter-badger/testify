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

import com.fitbur.testify.Mock;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestInjector;
import com.fitbur.testify.TestReifier;
import com.fitbur.testify.descriptor.DescriptorKey;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.descriptor.ParameterDescriptor;
import com.fitbur.testify.di.ServiceLocator;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * An integration test injector implementation that injects fields annotated
 * with {@link Mock} that does some smart name/type based detection on the cut
 * class constructor.
 *
 * @author saden
 */
public class IntegrationTypeMockInjector implements TestInjector {

    private final TestContext context;
    private final TestReifier testReifier;
    private final ServiceLocator appContext;
    private final FieldDescriptor fieldDescriptor;
    private final Object[] arguments;

    public IntegrationTypeMockInjector(TestContext context,
            TestReifier testReifier,
            ServiceLocator appContext,
            FieldDescriptor fieldDescriptor,
            Object[] arguments) {
        this.context = context;
        this.testReifier = testReifier;
        this.appContext = appContext;
        this.fieldDescriptor = fieldDescriptor;
        this.arguments = arguments;
    }

    @Override
    public void inject() {
        Map<DescriptorKey, ParameterDescriptor> parameterDescriptors = context.getParamaterDescriptors();
        Field field = fieldDescriptor.getField();

        Mock mock = fieldDescriptor.getMock().get();
        Type fieldType = field.getGenericType();
        String fieldName = field.getName();
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
            for (ParameterDescriptor descriptor : descriptors) {
                Parameter parameter = descriptor.getParameter();
                Type parameterType = parameter.getParameterizedType();
                Integer index = descriptor.getIndex();

                if (arguments[index] != null) {
                    continue;
                }

                if (parameterType.equals(fieldType)) {
                    Object instance = testReifier.reifyField(fieldDescriptor, descriptor);
                    arguments[index] = instance;
                    break;
                }

            }
        }

    }

}
