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
package com.fitbur.testify.integration;

import com.fitbur.testify.Cut;
import com.fitbur.testify.Mock;
import com.fitbur.testify.Real;
import com.fitbur.testify.TestReifier;
import com.fitbur.testify.descriptor.CutDescriptor;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.descriptor.ParameterDescriptor;
import com.fitbur.testify.di.ServiceDescriptor;
import com.fitbur.testify.di.ServiceDescriptorBuilder;
import com.fitbur.testify.di.ServiceLocator;
import com.fitbur.testify.di.ServiceScope;
import com.google.common.reflect.TypeToken;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Answers.RETURNS_DEFAULTS;
import org.mockito.MockSettings;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.withSettings;

/**
 * An implementation of test reifier that creates instances of test class fields
 * and class under test and adds them to the spring application context.
 *
 * @author saden
 */
public class IntegrationTestReifier implements TestReifier {

    private final ServiceLocator locator;
    private final Object testInstance;

    public IntegrationTestReifier(ServiceLocator locator, Object testInstance) {
        this.locator = locator;
        this.testInstance = testInstance;
    }

    @Override
    public Object reifyField(FieldDescriptor descriptor, ParameterDescriptor parameterDescriptor) {
        return AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                Field field = descriptor.getField();
                Type fieldType = field.getGenericType();
                field.setAccessible(true);
                Object instance = field.get(testInstance);

                Optional<Mock> optMock = descriptor.getMock();
                Optional<Real> optReal = descriptor.getAnnotation(Real.class);
                Optional<Inject> optInject = descriptor.getAnnotation(Inject.class);
                if (optMock.isPresent()) {
                    Mock mock = optMock.get();
                    //if the field value is set then create a mock otherwise create a mock
                    //that delegates to the value
                    if (instance == null) {
                        MockSettings settings = withSettings()
                                .defaultAnswer(RETURNS_DEFAULTS);

                        instance = mock(field.getType(), settings);
                    } else {
                        instance = mock(field.getType(), delegatesTo(instance));
                    }

                } else if (optReal.isPresent() || optInject.isPresent()) {
                    instance = locator.getService(fieldType, descriptor.getAnnotations());

                    if (optReal.isPresent() && optReal.get().value()) {
                        instance = mock(instance.getClass(), delegatesTo(instance));
                    }
                }

                field.set(testInstance, instance);
                descriptor.setInstance(instance);
                parameterDescriptor.setInstance(instance);

                return instance;
            } catch (IllegalAccessException | IllegalArgumentException e) {
                throw new IllegalStateException(e);
            }
        });

    }

    @Override
    public Object reifyCut(CutDescriptor descriptor, Object[] arguments) {
        return AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                Cut cut = descriptor.getCut().get();
                Field field = descriptor.getField();
                Type fieldType = field.getGenericType();
                String fieldName = field.getName();

                field.setAccessible(true);

                Constructor<?> constructor = descriptor.getConstructor();
                constructor.setAccessible(true);

                TypeToken<?> token = TypeToken.of(fieldType);
                Class rawType;

                if (token.isSupertypeOf(Provider.class) || token.isSupertypeOf(Optional.class)) {
                    rawType = token.getRawType();
                } else {
                    rawType = (Class) fieldType;
                }

                ServiceDescriptor serviceDescriptor = new ServiceDescriptorBuilder()
                        .name(fieldName)
                        .type(rawType)
                        .injectable(false)
                        .scope(ServiceScope.PROTOTYPE)
                        .primary(true)
                        .lazy(true)
                        .build();

                locator.addService(serviceDescriptor);

                Object instance = locator.getServiceWith(rawType, arguments);

                if (cut.value()) {
                    instance = spy(instance);
                }

                field.set(testInstance, instance);
                descriptor.setInstance(instance);
                descriptor.setArguments(arguments);

                return instance;
            } catch (SecurityException |
                    IllegalAccessException |
                    IllegalArgumentException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @Override
    public void reifyTest(Set< FieldDescriptor> fieldDescriptors) {

        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {

            fieldDescriptors
                    .parallelStream()
                    .forEach(p -> {
                        try {
                            Field field = p.getField();
                            Optional<Real> real = p.getAnnotation(Real.class);
                            Optional<Inject> inject = p.getAnnotation(Inject.class);

                            Object instance = null;

                            if (real.isPresent() && real.get().value()) {
                                Object service = locator.getService(p.getType());
                                instance = mock(p.getType(), delegatesTo(service));
                            } else if (real.isPresent() || inject.isPresent()) {
                                instance = locator.getService(p.getType());
                            }

                            field.setAccessible(true);
                            field.set(testInstance, instance);
                        } catch (SecurityException |
                                IllegalAccessException |
                                IllegalArgumentException e) {
                            throw new IllegalStateException(e);
                        }
                    });

            return null;
        });

    }

}
