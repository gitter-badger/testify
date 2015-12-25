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

import com.fitbur.guava.common.reflect.TypeToken;
import com.fitbur.testify.Fake;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestInjector;
import com.fitbur.testify.TestReifier;
import com.fitbur.testify.descriptor.DescriptorKey;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.descriptor.ParameterDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * An integration test injector implementation that injects fields annotated
 * with {@link com.fitbur.testify.Fake} that does some smart name/type based
 * detection on the cut * class constructor.
 *
 * @author saden
 */
public class IntegrationTypeFakeInjector implements TestInjector {

    private final TestContext context;
    private final TestReifier testReifier;
    private final Object[] arguments;

    public IntegrationTypeFakeInjector(TestContext context,
            TestReifier testReifier,
            Object[] arguments) {
        this.context = context;
        this.testReifier = testReifier;
        this.arguments = arguments;
    }

    @Override
    public void inject(FieldDescriptor descriptor) {
        Fake fake = descriptor.getAnnotation(Fake.class).get();
        String fakeName = fake.name();
        Integer fakeIndex = fake.index();

        if (!fakeName.isEmpty() || fakeIndex != -1) {
            return;
        }

        Map<DescriptorKey, ParameterDescriptor> parameterDescriptors = context.getParamaterDescriptors();
        Field field = descriptor.getField();

        Type fieldType = descriptor.getGenericType();
        String fieldName = field.getName();
        DescriptorKey descriptorKey = new DescriptorKey(fieldType, fieldName);

        //if there is a parameter descriptor that matches the field then lets use that
        if (parameterDescriptors.containsKey(descriptorKey)) {
            ParameterDescriptor parameterDescriptor = parameterDescriptors.get(descriptorKey);
            Integer index = parameterDescriptor.getIndex();

            Object instance = testReifier.reifyField(descriptor, parameterDescriptor);
            arguments[index] = instance;
        } else {
            TypeToken token = TypeToken.of(fieldType);

            //otherwise find the right parameter based on the type of the field
            Collection<ParameterDescriptor> descriptors = parameterDescriptors.values();
            for (ParameterDescriptor paramDescriptor : descriptors) {
                Parameter parameter = paramDescriptor.getParameter();
                Type paramType = parameter.getParameterizedType();
                Integer index = paramDescriptor.getIndex();

                if (arguments[index] != null) {
                    continue;
                }

                if (token.isSubtypeOf(paramType)) {
                    Object instance = testReifier.reifyField(descriptor, paramDescriptor);
                    arguments[index] = instance;
                    break;
                }

            }
        }

    }

}
