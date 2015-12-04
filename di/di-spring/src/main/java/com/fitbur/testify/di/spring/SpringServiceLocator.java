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
package com.fitbur.testify.di.spring;

import com.fitbur.testify.di.ServiceDescriptor;
import com.fitbur.testify.di.ServiceLocator;
import com.fitbur.testify.di.ServiceProvider;
import com.fitbur.testify.di.ServiceScope;
import static com.fitbur.testify.di.ServiceScope.REQUEST;
import static com.fitbur.testify.di.ServiceScope.SESSION;
import static com.fitbur.testify.di.ServiceScope.SINGLETON;
import static com.google.common.base.Preconditions.checkState;
import com.google.common.reflect.TypeToken;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static java.util.Optional.empty;
import java.util.Set;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import javax.inject.Named;
import javax.inject.Provider;
import org.springframework.beans.factory.annotation.Qualifier;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_APPLICATION;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import static org.springframework.beans.factory.support.AbstractBeanDefinition.AUTOWIRE_BY_TYPE;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * A spring implementation of test service locator. It provides the ability to
 * work with AnnotationConfigApplication context to create and load services
 * dynamically.
 *
 * @author saden
 */
public class SpringServiceLocator implements ServiceLocator {

    public final AnnotationConfigApplicationContext context;

    public SpringServiceLocator(AnnotationConfigApplicationContext context) {
        this.context = context;
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
        if (isActive() && context.isRunning()) {
            context.close();
        }
    }

    @Override
    public void reload() {
        if (!isActive()) {
            context.refresh();
        }
    }

    @Override
    public boolean isActive() {
        return context.isActive();
    }

    @Override
    public AnnotationConfigApplicationContext getContext() {
        return context;
    }

    @Override
    public String getName() {
        return context.getId();
    }

    @Override
    public <T> T getService(String name) {
        return (T) context.getBean(name);
    }

    @Override
    public <T> T getService(Type type) {
        TypeToken<?> token = TypeToken.of(type);
        Class<?> rawType = token.getRawType();
        Class<Provider> providerType = Provider.class;
        Object instance;

        if (token.isSubtypeOf(providerType)) {
            TypeVariable<Class<Provider>> paramType = providerType.getTypeParameters()[0];
            rawType = token.resolveType(paramType).getRawType();
            instance = new ServiceProvider(this, rawType);
        } else {
            instance = context.getBean(rawType);
        }

        return (T) instance;
    }

    @Override
    public <T> T getService(Type type, String name) {
        TypeToken<?> token = TypeToken.of(type);

        return (T) context.getBean(name, token.getRawType());
    }

    @Override
    public <T> T getService(Type type, Set<? extends Annotation> annotations) {
        TypeToken<?> token = TypeToken.of(type);

        Object instance;
        Optional<String> beanName = empty();
        Class rawType = token.getRawType();
        Class<Provider> providerClass = Provider.class;

        Optional<Qualifier> qualifier = annotations.parallelStream()
                .filter(p -> p.annotationType().equals(Qualifier.class))
                .map(Qualifier.class::cast)
                .findFirst();

        Optional<Named> named = annotations.parallelStream()
                .filter(p -> p.annotationType().equals(Named.class))
                .map(Named.class::cast)
                .findFirst();

        if (named.isPresent()) {
            beanName = Optional.of(named.get().value());
        }

        if (qualifier.isPresent()) {
            beanName = Optional.of(qualifier.get().value());
        }

        if (beanName.isPresent()) {
            String name = beanName.get();
            if (token.isSubtypeOf(providerClass)) {
                TypeVariable<Class<Provider>> providerParamType = providerClass.getTypeParameters()[0];
                Class paramType = token.resolveType(providerParamType).getRawType();
                instance = new ServiceProvider(this, name, paramType);
            } else {
                instance = context.getBean(name, rawType);
            }
        } else if (token.isSubtypeOf(providerClass)) {
            TypeVariable<Class<Provider>> providerParamType = providerClass.getTypeParameters()[0];
            Class paramType = token.resolveType(providerParamType).getRawType();
            instance = new ServiceProvider(this, paramType);
        } else if (Map.class.isAssignableFrom(rawType)) {
            instance = context.getBeansOfType(rawType);
        } else if (Set.class.isAssignableFrom(rawType)) {
            instance = context.getBeansOfType(rawType).values().stream()
                    .collect(toSet());
        } else if (List.class.isAssignableFrom(rawType)) {
            instance = context.getBeansOfType(rawType).values().stream()
                    .collect(toList());
        } else {
            instance = context.getBean(rawType);
        }

        return (T) instance;
    }

    @Override
    public <T> T getServiceWith(Class< T> type, Object... arguments) {
        return context.getBean(type, arguments);
    }

    @Override
    public <T> T getServiceWith(String name, Object... arguments) {
        return (T) context.getBean(name, arguments);
    }

    @Override
    public void addService(Class<?> type) {
        context.register(type);
    }

    @Override
    public void addService(ServiceDescriptor descriptor) {
        checkState(!context.containsBeanDefinition(descriptor.getName()),
                "Service with the name '%s' already exists.",
                descriptor.getName());

        GenericBeanDefinition bean = new GenericBeanDefinition();
        bean.setBeanClass(descriptor.getType());
        bean.setAutowireCandidate(descriptor.getInjectable());
        bean.setPrimary(descriptor.getPrimary());
        bean.setLazyInit(descriptor.getLazy());
        bean.setRole(ROLE_APPLICATION);
        bean.setAutowireMode(AUTOWIRE_BY_TYPE);
        ConstructorArgumentValues values = new ConstructorArgumentValues();

        for (Object value : descriptor.getArguments()) {
            values.addGenericArgumentValue(value);
        }

        bean.setConstructorArgumentValues(values);

        ServiceScope scope = descriptor.getScope();
        switch (scope) {
            case PROTOTYPE:
                bean.setScope("prototype");
                break;
            case SINGLETON:
                bean.setScope("singleton");
                break;
            case REQUEST:
                bean.setScope("request");
                break;
            case SESSION:
                bean.setScope("session");
                break;
            case APPLICATION:
                bean.setScope("application");
                break;
            default:
                checkState(false, "Scope '{}' is not supported by Spring IoC.", scope.name());

        }

        context.registerBeanDefinition(descriptor.getName(), bean);
    }

    @Override
    public void removeService(ServiceDescriptor descriptor) {
        context.removeBeanDefinition(descriptor.getName());
    }

    @Override
    public void removeService(String name) {
        context.removeBeanDefinition(name);
    }

    @Override
    public void addModule(Class<?> type) {
        context.register(type);
    }

}
