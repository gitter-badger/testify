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

import static com.fitbur.guava.common.base.Preconditions.checkState;
import com.fitbur.guava.common.reflect.TypeToken;
import com.fitbur.testify.di.ServiceAnnotations;
import com.fitbur.testify.di.ServiceDescriptor;
import com.fitbur.testify.di.ServiceLocator;
import com.fitbur.testify.di.ServiceProvider;
import com.fitbur.testify.di.ServiceScope;
import static com.fitbur.testify.di.ServiceScope.REQUEST;
import static com.fitbur.testify.di.ServiceScope.SESSION;
import static com.fitbur.testify.di.ServiceScope.SINGLETON;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import java.util.Set;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import javax.inject.Named;
import javax.inject.Provider;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_APPLICATION;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import static org.springframework.beans.factory.support.AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR;
import static org.springframework.beans.factory.support.AbstractBeanDefinition.AUTOWIRE_NO;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigRegistry;

/**
 * A spring implementation of test service locator. It provides the ability to
 * work with AnnotationConfigApplication context to create and load services
 * dynamically.
 *
 * @author saden
 */
public class SpringServiceLocator implements ServiceLocator {

    private final ConfigurableApplicationContext context;
    private final ServiceAnnotations serviceAnnotations;

    public SpringServiceLocator(ConfigurableApplicationContext context, ServiceAnnotations annotations) {
        this.context = context;
        this.serviceAnnotations = annotations;
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
    public ConfigurableApplicationContext getContext() {
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
        Class rawType = token.getRawType();
        Object instance;

        if (token.isSubtypeOf(Provider.class)) {
            TypeVariable<Class<Provider>> paramType = Provider.class.getTypeParameters()[0];
            rawType = token.resolveType(paramType).getRawType();
            instance = new ServiceProvider(this, rawType);
        } else if (token.isSubtypeOf(Optional.class)) {
            TypeVariable<Class<Optional>> paramType = Optional.class.getTypeParameters()[0];
            rawType = token.resolveType(paramType).getRawType();
            instance = Optional.ofNullable(context.getBean(rawType));
        } else if (token.isSubtypeOf(Map.class)) {
            TypeVariable<Class<Map>> valueType = Map.class.getTypeParameters()[1];
            rawType = token.resolveType(valueType).getRawType();
            instance = context.getBeansOfType(rawType);
        } else if (token.isSubtypeOf(Set.class)) {
            TypeVariable<Class<Set>> valueType = Set.class.getTypeParameters()[0];
            rawType = token.resolveType(valueType).getRawType();
            instance = context.getBeansOfType(rawType)
                    .values()
                    .stream()
                    .collect(toSet());
        } else if (token.isSubtypeOf(List.class)) {
            TypeVariable<Class<List>> valueType = List.class.getTypeParameters()[0];
            rawType = token.resolveType(valueType).getRawType();
            instance = context.getBeansOfType(rawType)
                    .values()
                    .stream()
                    .collect(toList());
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
        Optional<Class<? extends Annotation>> beanAnnotation = empty();
        Class rawType = token.getRawType();

        Optional<String> beanName = annotations.parallelStream()
                .filter(p -> p.annotationType().equals(Qualifier.class))
                .map(Qualifier.class::cast)
                .map(Qualifier::value)
                .findFirst();

        if (!beanName.isPresent()) {
            beanName = annotations.parallelStream()
                    .filter(p -> p.annotationType().equals(Named.class))
                    .map(Named.class::cast)
                    .map(Named::value)
                    .findFirst();
        }

        if (!beanName.isPresent()) {
            Set<Class<? extends Annotation>> customQualfiers
                    = serviceAnnotations.getCustomQualifiers();

            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();

                for (Class<? extends Annotation> customQualfier : customQualfiers) {
                    if (annotationType.isAnnotationPresent(customQualfier)
                            || annotationType.equals(Qualifier.class)) {
                        beanAnnotation = of(annotationType);
                        break;
                    }
                }

                if (beanAnnotation.isPresent()) {
                    break;
                }
            }
        }

        if (beanName.isPresent()) {
            String name = beanName.get();
            if (token.isSubtypeOf(Provider.class)) {
                TypeVariable<Class<Provider>> paramType = Provider.class.getTypeParameters()[0];
                rawType = token.resolveType(paramType).getRawType();
                instance = new ServiceProvider(this, name, rawType);
            } else if (token.isSubtypeOf(Optional.class)) {
                TypeVariable<Class<Optional>> paramType = Optional.class.getTypeParameters()[0];
                rawType = token.resolveType(paramType).getRawType();
                instance = Optional.ofNullable(context.getBean(name, rawType));
            } else {
                instance = context.getBean(name, rawType);
            }
        } else if (beanAnnotation.isPresent()) {
            Map<String, Object> beans = context.getBeansWithAnnotation(beanAnnotation.get());
            instance = beans.entrySet().parallelStream().map(p -> {
                Object value;
                if (token.isSubtypeOf(Provider.class)) {
                    value = new ServiceProvider(this, p.getKey(), p.getValue().getClass());
                } else if (token.isSubtypeOf(Optional.class)) {
                    value = of(p.getValue());
                } else {
                    value = p.getValue();
                }

                return value;
            }).findFirst().get();
        } else {
            instance = getService(type);
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
        ((AnnotationConfigRegistry) context).register(type);
    }

    @Override
    public void addService(ServiceDescriptor descriptor) {
        checkState(!context.containsBeanDefinition(descriptor.getName()),
                "Service with the name '%s' already exists.",
                descriptor.getName());

        GenericBeanDefinition bean = new GenericBeanDefinition();
        bean.setBeanClass(descriptor.getType());
        bean.setAutowireCandidate(descriptor.getDiscoverable());
        bean.setPrimary(descriptor.getPrimary());
        bean.setLazyInit(descriptor.getLazy());
        bean.setRole(ROLE_APPLICATION);

        if (descriptor.getInjectable()) {
            bean.setAutowireMode(AUTOWIRE_CONSTRUCTOR);
        } else {
            bean.setAutowireMode(AUTOWIRE_NO);
            ConstructorArgumentValues values = new ConstructorArgumentValues();

            Object[] arguments = descriptor.getArguments();
            for (int i = 0; i < arguments.length; i++) {
                Object arg = arguments[i];

                if (arg == null) {
                    //TODO: warn user that the argument was not specified and there
                    //for the real instance will be injected.
                    continue;
                }

                values.addIndexedArgumentValue(i, arg);
            }

            bean.setConstructorArgumentValues(values);
        }

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
        ((DefaultListableBeanFactory) context.getBeanFactory())
                .registerBeanDefinition(descriptor.getName(), bean);
    }

    @Override
    public void addConstant(String name, Object instance) {
        ((DefaultListableBeanFactory) context.getBeanFactory()).registerSingleton(name, instance);
    }

    @Override
    public void replaceWithConstant(Set<Class<?>> contracts, String name, Object instance) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();

        for (Class<?> contract : contracts) {
            String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, contract, true, false);
            for (String beanName : beanNames) {
                beanFactory.removeBeanDefinition(beanName);
            }
        }

        beanFactory.registerSingleton(name, instance);
    }

    @Override
    public void removeService(ServiceDescriptor descriptor) {
        ((BeanDefinitionRegistry) context).removeBeanDefinition(descriptor.getName());
    }

    @Override
    public void removeService(String name) {
        ((BeanDefinitionRegistry) context).removeBeanDefinition(name);
    }

    @Override
    public void addModule(Class<?> type) {
        ((AnnotationConfigRegistry) context).register(type);
    }

    @Override
    public void scanPackage(String packageName) {
        ((AnnotationConfigRegistry) context).scan(packageName);
    }

    @Override
    public ServiceAnnotations getServiceAnnotations() {
        return serviceAnnotations;
    }

}
