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

import static com.fitbur.guava.common.base.Preconditions.checkState;
import com.fitbur.testify.Fake;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestInjector;
import com.fitbur.testify.TestReifier;
import com.fitbur.testify.descriptor.DescriptorKey;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.descriptor.ParameterDescriptor;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

/**
 * A unit test injector implementation that injects fields annotated with
 * {@link Fake} which specify the index of the fake on the class under test
 * constructor.
 *
 * @author saden
 */
public class UnitIndexFakeInjector implements TestInjector {

    private final TestContext context;
    private final TestReifier testReifier;
    private final Object[] arguments;

    public UnitIndexFakeInjector(TestContext context,
            TestReifier testReifier,
            Object[] arguments) {
        this.context = context;
        this.testReifier = testReifier;
        this.arguments = arguments;
    }

    @Override
    public void inject(FieldDescriptor descriptor) {
        Fake fake = descriptor.getAnnotation(Fake.class).get();
        Integer fakeIndex = fake.index();

        if (fake.index() == -1) {
            return;
        }

        Map<DescriptorKey, ParameterDescriptor> parameterDescriptors = context.getParamaterDescriptors();

        Optional<ParameterDescriptor> optional = parameterDescriptors.values()
                .parallelStream()
                .filter(p -> fakeIndex.equals(p.getIndex()))
                .findFirst();

        ParameterDescriptor paramDescriptor = optional.get();
        Parameter parameter = paramDescriptor.getParameter();

        String testClassName = context.getTestClassName();
        Type fieldType = descriptor.getGenericType();
        String fieldTypeName = descriptor.getTypeName();
        String fieldName = descriptor.getName();
        Type paramType = parameter.getParameterizedType();

        checkState(fieldType.equals(paramType),
                "Can not fake field '%s#%s'. Test class field type '%s' and class "
                + "under test constructor parameter type '%s' at index '%d' do "
                + "not match.",
                testClassName, fieldName, fieldTypeName, paramType, fakeIndex
        );

        checkState(arguments[fakeIndex] == null,
                "Can not fake field '%s#%s'. Multipe test class fields are "
                + "annotated with @Fake(index=%d). Please insure the @Fake "
                + "annotations have unqiue indexes.",
                testClassName, fieldName, fakeIndex
        );

        arguments[fakeIndex] = testReifier.reifyField(descriptor, paramDescriptor);

    }

}
