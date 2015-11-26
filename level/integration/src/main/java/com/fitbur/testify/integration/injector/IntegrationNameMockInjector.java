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
import static com.google.common.base.Preconditions.checkArgument;
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
    private final FieldDescriptor fieldDescriptor;
    private final Object[] arguments;

    public IntegrationNameMockInjector(TestContext context,
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
        String testClassName = context.getTestClassName();
        Type fieldType = fieldDescriptor.getGenericType();
        String fieldTypeName = fieldDescriptor.getTypeName();
        String fieldName = fieldDescriptor.getName();
        String cutTypeName = context.getCutDescriptor().getTypeName();

        Mock mock = fieldDescriptor.getMock().get();
        String mockName = mock.name();
        DescriptorKey descriptorKey = new DescriptorKey(fieldType, mockName);

        ParameterDescriptor paramDescriptor = parameterDescriptors.get(descriptorKey);

        checkArgument(paramDescriptor != null,
                "Can not mock field '%s#%s'. Could not find constructor argument "
                + "with the name '%s' in the '%s'. Please note that name based auto "
                + "detection will only work if your code is compiled with debug "
                + "information (javac -parameters or javac -g:vars).",
                testClassName, fieldName, mockName, cutTypeName);

        Parameter parameter = paramDescriptor.getParameter();
        Integer paramIndex = paramDescriptor.getIndex();
        Type paramType = parameter.getParameterizedType();

        checkArgument(fieldType.equals(paramType),
                "Can not mock field '%s#%s'. The test clas field and the class "
                + "under test constructor argument have the name '%s' but are "
                + "not the same type. Please insure that the field type (%s) "
                + "and paramater type (%s) are the same.",
                testClassName, fieldName, mockName, fieldTypeName, paramType
        );

        checkArgument(arguments[paramIndex] == null,
                "Can not mock field '%s#%s'. Multipe test class fields have the "
                + "same name of '%s'",
                testClassName, fieldName, mockName);

        arguments[paramIndex] = testReifier.reifyField(fieldDescriptor, paramDescriptor);
    }

}
