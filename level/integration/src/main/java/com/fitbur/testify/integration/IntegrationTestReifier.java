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
import com.fitbur.testify.di.ServiceProvider;
import com.fitbur.testify.di.ServiceScope;
import com.google.common.reflect.TypeToken;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Provider;
import static org.mockito.AdditionalAnswers.delegatesTo;
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

    private final ServiceLocator appContext;
    private final Object testInstance;

    public IntegrationTestReifier(ServiceLocator appContext, Object testInstance) {
        this.appContext = appContext;
        this.testInstance = testInstance;
    }

    @Override
    public Object reifyField(FieldDescriptor fieldDescriptor, ParameterDescriptor parameterDescriptor) {
        return AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                Field field = fieldDescriptor.getField();
                Type genericFieldTYpe = field.getGenericType();
                field.setAccessible(true);
                Object instance = field.get(testInstance);

                Optional<Mock> optMock = fieldDescriptor.getMock();
                Optional<Real> optReal = fieldDescriptor.getAnnotation(Real.class);
                Optional<Inject> optInject = fieldDescriptor.getAnnotation(Inject.class);
                if (optMock.isPresent()) {
                    Mock mock = optMock.get();
                    //if the field value is set then create a mock otherwise create a mock
                    //that delegates to the value
                    if (instance == null) {
                        MockSettings settings = withSettings()
                                .defaultAnswer(mock.answer());

                        if (mock.extraInterfaces().length > 0) {
                            settings.extraInterfaces(mock.extraInterfaces());

                        }

                        instance = mock(field.getType(), settings);
                    } else {
                        instance = mock(field.getType(), delegatesTo(instance));
                    }

                } else if (optReal.isPresent() || optInject.isPresent()) {
                    TypeToken<?> token = TypeToken.of(genericFieldTYpe);
                    Class rawType;

                    if (token.isSubtypeOf(Provider.class)) {
                        rawType = token.resolveType(Provider.class.getTypeParameters()[0]).getRawType();
                        instance = new ServiceProvider(appContext, rawType);
                    } else {
                        rawType = (Class) genericFieldTYpe;
                        instance = appContext.getService(rawType);
                    }

                    if (optReal.isPresent() && optReal.get().value()) {
                        instance = mock(instance.getClass(), delegatesTo(instance));
                    }
                }

                field.set(testInstance, instance);
                fieldDescriptor.setInstance(instance);
                parameterDescriptor.setInstance(instance);

                return instance;
            } catch (IllegalAccessException | IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
        });

    }

    @Override
    public Object reifyCut(CutDescriptor cutDescriptor, Object[] arguments) {
        return AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                Cut cut = cutDescriptor.getCut();
                Field field = cutDescriptor.getField();
                Type fieldType = field.getGenericType();
                String fieldName = field.getName();

                field.setAccessible(true);

                Constructor<?> constructor = cutDescriptor.getConstructor();
                constructor.setAccessible(true);

                TypeToken<?> token = TypeToken.of(fieldType);
                Class rawType;

                if (token.isSupertypeOf(Provider.class) || token.isSupertypeOf(Optional.class)) {
                    rawType = token.getRawType();
                } else {
                    rawType = (Class) fieldType;
                }

                ServiceDescriptor descriptor = new ServiceDescriptorBuilder()
                        .name(fieldName)
                        .type(rawType)
                        .injectable(false)
                        .scope(ServiceScope.PROTOTYPE)
                        .primary(true)
                        .lazy(true)
                        .build();

                appContext.addService(descriptor);

                Object instance = appContext.getServiceWith(rawType, arguments);

                if (cut.value()) {
                    instance = spy(instance);
                }

                field.set(testInstance, instance);

                return instance;
            } catch (SecurityException |
                    IllegalAccessException |
                    IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void reifyTest(Set< FieldDescriptor> fieldDescriptors) {
        Stream<Field> stream = fieldDescriptors
                .parallelStream()
                .map(FieldDescriptor::getField);

        stream.forEach(p -> {
            Object spy;
            Real real = p.getDeclaredAnnotation(Real.class);
            if (real != null && real.value()) {
                spy = spy(appContext.getService(p.getType()));
            } else {
                spy = appContext.getService(p.getType());
            }

            Object instance = spy;
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                try {
                    p.setAccessible(true);
                    p.set(testInstance, instance);

                    return instance;
                } catch (SecurityException |
                        IllegalAccessException |
                        IllegalArgumentException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

}
