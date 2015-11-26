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

import com.fitbur.testify.Mock;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestInjector;
import com.fitbur.testify.TestReifier;
import com.fitbur.testify.descriptor.DescriptorKey;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.descriptor.ParameterDescriptor;
import static com.google.common.base.Preconditions.checkState;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

/**
 * A unit test injector implementation that injects fields annotated with
 * {@link Mock} which specify the index of the mock on the class under test
 * constructor.
 *
 * @author saden
 */
public class UnitIndexMockInjector implements TestInjector {

    private final TestContext context;
    private final TestReifier testReifier;
    private final FieldDescriptor fieldDescriptor;
    private final Object[] arguments;

    public UnitIndexMockInjector(TestContext context,
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

        Mock mock = fieldDescriptor.getMock().get();
        Integer mockIndex = mock.index();
        Optional<ParameterDescriptor> optional = parameterDescriptors.values()
                .parallelStream()
                .filter(p -> mockIndex.equals(p.getIndex()))
                .findFirst();

        ParameterDescriptor paramDescriptor = optional.get();
        Parameter parameter = paramDescriptor.getParameter();

        String testClassName = context.getTestClassName();
        Type fieldType = fieldDescriptor.getGenericType();
        String fieldTypeName = fieldDescriptor.getTypeName();
        String fieldName = fieldDescriptor.getName();
        Type parameterType = parameter.getParameterizedType();

        checkState(fieldType.equals(parameterType),
                "Can not mock field '%s#%s'. Test class field type '%s' and class "
                + "under test constructor parameter type '%s' at index '%d' do "
                + "not match.",
                testClassName, fieldName, fieldTypeName, parameterType, mockIndex
        );

        checkState(arguments[mockIndex] == null,
                "Can not mock field '%s#%s'. Multipe test class fields are "
                + "annotated with @Mock(index=%d). Please insure the @Mock "
                + "annotations have unqiue indexes.",
                testClassName, fieldName, mockIndex
        );

        arguments[mockIndex] = testReifier.reifyField(fieldDescriptor, paramDescriptor);
    }

}
