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

import com.fitbur.testify.Cut;
import com.fitbur.testify.Mock;
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
import static org.mockito.Answers.RETURNS_DEFAULTS;
import org.mockito.MockSettings;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.withSettings;
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
    public Object reifyField(FieldDescriptor fieldDescriptor, ParameterDescriptor parameterDescriptor) {
        return doPrivileged((PrivilegedAction) () -> {
            try {
                Object instance = null;
                Optional<Mock> optMock = fieldDescriptor.getMock();

                if (optMock.isPresent()) {
                    Mock mock = optMock.get();
                    Field field = fieldDescriptor.getField();
                    field.setAccessible(true);
                    Object value = field.get(testInstance);
                    //if the field value is null create a new mock
                    if (value == null || MOCK_UTIL.isMock(value) || MOCK_UTIL.isSpy(value)) {
                        MockSettings settings = withSettings()
                                .defaultAnswer(RETURNS_DEFAULTS);

                        instance = mock(field.getType(), settings);
                    } else {
                        //otherwise create a mock that delegates to the value
                        instance = mock(field.getType(), delegatesTo(value));
                    }

                    field.set(testInstance, instance);
                    fieldDescriptor.setInstance(instance);
                    parameterDescriptor.setInstance(instance);
                }

                return instance;
            } catch (IllegalAccessException | IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
        });

    }

    @Override
    public Object reifyCut(CutDescriptor cutDescriptor, Object[] arguments) {
        return doPrivileged((PrivilegedAction) () -> {
            try {
                Cut cut = cutDescriptor.getCut();
                Field field = cutDescriptor.getField();
                field.setAccessible(true);
                Constructor<?> constructor = cutDescriptor.getConstructor();
                constructor.setAccessible(true);

                Object instance = constructor.newInstance(arguments);

                if (cut.value()) {
                    instance = spy(instance);
                }

                field.set(testInstance, instance);

                return instance;
            } catch (SecurityException |
                    InstantiationException |
                    IllegalAccessException |
                    IllegalArgumentException |
                    InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
