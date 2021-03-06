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

import static com.fitbur.guava.common.base.Preconditions.checkState;
import com.fitbur.testify.Cut;
import com.fitbur.testify.Fake;
import com.fitbur.testify.TestReifier;
import com.fitbur.testify.descriptor.CutDescriptor;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.descriptor.ParameterDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import static java.security.AccessController.doPrivileged;
import java.security.PrivilegedAction;
import java.util.Optional;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import org.mockito.internal.util.MockUtil;

/**
 * An implementation of test reifier that creates instances of test class fields
 * and class under test.
 *
 * @author saden
 */
public class UnitTestReifier implements TestReifier {

    private static final MockUtil MOCK_UTIL = new MockUtil();
    private final Object testInstance;

    public UnitTestReifier(Object testInstance) {
        this.testInstance = testInstance;
    }

    @Override
    public Object reifyField(FieldDescriptor fieldDescriptor, ParameterDescriptor paramDescriptor) {
        return doPrivileged((PrivilegedAction) () -> {
            try {
                Object instance = null;
                Optional<Fake> mock = fieldDescriptor.getAnnotation(Fake.class);

                if (mock.isPresent()) {
                    Field field = fieldDescriptor.getField();
                    field.setAccessible(true);

                    Object value = field.get(testInstance);
                    //if the field value is null create a new mock
                    if (value == null) {
                        instance = mock(field.getType());
                    } else if (MOCK_UTIL.isMock(value)) {
                        //if the value is already a mock just use it.
                        instance = value;
                    } else {
                        //otherwise create a mock that delegates to the value
                        instance = mock(field.getType(), delegatesTo(value));
                    }

                    field.set(testInstance, instance);
                    fieldDescriptor.setInstance(instance);
                    paramDescriptor.setInstance(instance);
                }

                return instance;
            } catch (IllegalAccessException | IllegalArgumentException e) {
                checkState(false,
                        "Field '%s' in test class '%s' is not accessible.\n",
                        fieldDescriptor.getName(), testInstance.getClass().getSimpleName(), e.getMessage());
                throw new RuntimeException(e);
            }
        });

    }

    @Override
    public Object reifyCut(CutDescriptor descriptor, Object[] arguments) {
        return doPrivileged((PrivilegedAction) () -> {
            try {
                Cut cut = descriptor.getCut().get();
                Field field = descriptor.getField();
                field.setAccessible(true);
                Constructor<?> constructor = descriptor.getConstructor();
                constructor.setAccessible(true);

                Object instance = constructor.newInstance(arguments);

                if (cut.value()) {
                    instance = spy(instance);
                }

                field.set(testInstance, instance);
                descriptor.setInstance(instance);
                descriptor.setArguments(arguments);

                return instance;
            } catch (InstantiationException |
                    IllegalAccessException |
                    InvocationTargetException e) {
                checkState(false,
                        "Cut '%s' in test class '%s' could not be created.\n%s",
                        descriptor.getName(), testInstance.getClass().getSimpleName(), e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}
