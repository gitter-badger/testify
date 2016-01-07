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
package com.fitbur.testify.system.injector;

import static com.fitbur.guava.common.base.Preconditions.checkArgument;
import com.fitbur.guava.common.reflect.TypeToken;
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

/**
 * An integration test injector implementation that injects fields annotated
 * with {@link Fake} which specify the parameter name of the fake on the class
 * under test constructor.
 *
 * @author saden
 */
public class SystemNameFakeInjector implements TestInjector {

    private final TestContext context;
    private final TestReifier testReifier;
    private final Object[] arguments;

    public SystemNameFakeInjector(TestContext context,
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

        if (fakeName.isEmpty()) {
            return;
        }

        Map<DescriptorKey, ParameterDescriptor> parameterDescriptors = context.getParamaterDescriptors();
        String testClassName = context.getTestClassName();
        Type fieldType = descriptor.getGenericType();
        String fieldTypeName = descriptor.getTypeName();
        String fieldName = descriptor.getName();
        String cutTypeName = context.getCutDescriptor().getTypeName();

        DescriptorKey descriptorKey = new DescriptorKey(fieldType, fakeName);

        ParameterDescriptor paramDescriptor = parameterDescriptors.get(descriptorKey);

        checkArgument(paramDescriptor != null,
                "Can not fake field '%s#%s'. Could not find constructor argument "
                + "with the name '%s' in the '%s'. Please note that name based auto "
                + "detection will only work if your code is compiled with debug "
                + "information (javac -parameters or javac -g:vars).",
                testClassName, fieldName, fakeName, cutTypeName);

        Parameter parameter = paramDescriptor.getParameter();
        Integer paramIndex = paramDescriptor.getIndex();
        Type paramType = parameter.getParameterizedType();
        TypeToken token = TypeToken.of(fieldType);

        checkArgument(token.isSubtypeOf(paramType),
                "Can not fake field '%s#%s'. The test clas field and the class "
                + "under test constructor argument have the name '%s' but are "
                + "not the same type. Please insure that the field type (%s) "
                + "and paramater type (%s) are the same.",
                testClassName, fieldName, fakeName, fieldTypeName, paramType
        );

        checkArgument(arguments[paramIndex] == null,
                "Can not fake field '%s#%s'. Multipe test class fields have the "
                + "same name of '%s'",
                testClassName, fieldName, fakeName);

        arguments[paramIndex] = testReifier.reifyField(descriptor, paramDescriptor);

    }

}
