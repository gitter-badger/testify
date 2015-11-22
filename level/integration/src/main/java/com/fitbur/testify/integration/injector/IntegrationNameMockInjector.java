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
package com.fitbur.testify.integration.injector;

import com.fitbur.testify.Mock;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestInjector;
import com.fitbur.testify.TestReifier;
import com.fitbur.testify.descriptor.DescriptorKey;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.descriptor.ParameterDescriptor;
import com.fitbur.testify.di.ServiceLocator;
import static com.google.common.base.Preconditions.checkArgument;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * An integration test injector implementation that injects fields annotated
 * with {@link Mock} which specify the parameter name of the mock on the class
 * under test constructor.
 *
 * @author saden
 */
public class IntegrationNameMockInjector implements TestInjector {

    private final TestContext context;
    private final TestReifier testReifier;
    private final ServiceLocator appContext;
    private final FieldDescriptor fieldDescriptor;
    private final Object[] arguments;

    public IntegrationNameMockInjector(TestContext context,
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
        Type fieldType = field.getGenericType();

        Mock mock = fieldDescriptor.getMock().get();
        String mockName = mock.name();
        DescriptorKey descriptorKey = new DescriptorKey(fieldType, mockName);

        ParameterDescriptor parameterDescriptor = parameterDescriptors.get(descriptorKey);

        checkArgument(parameterDescriptor != null,
                "Can not mock field '%s'. Could not find class under test constructor "
                + "argument with the name '%s'.",
                field.getName(), mockName);

        Parameter parameter = parameterDescriptor.getParameter();
        Integer index = parameterDescriptor.getIndex();
        Type parameterType = parameter.getParameterizedType();

        checkArgument(fieldType.equals(parameterType),
                "Can not mock field '%s'. Test class field type '%s' and class under test "
                + "constructor parameter type '%s' with name '%s' do not match.",
                field.getName(), field.getGenericType(), parameterType, mockName
        );

        checkArgument(arguments[index] == null,
                "Can not mock field '%s'. Multipe test class fields have the same index of '%d'",
                field.getName(), index);

        Object instance = testReifier.reifyField(fieldDescriptor, parameterDescriptor);

        arguments[index] = instance;
    }

}
