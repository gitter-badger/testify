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
package com.fitbur.testify.di.spring;

import com.fitbur.testify.di.ServiceDescriptor;
import com.fitbur.testify.di.ServiceScope;
import com.fitbur.testify.di.TestServiceLocator;
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
public class SpringTestServiceLocator implements TestServiceLocator {

    public final AnnotationConfigApplicationContext context;

    public SpringTestServiceLocator(AnnotationConfigApplicationContext context) {
        this.context = context;
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
        if (this.context.isActive()) {
            this.context.close();
        }
    }

    @Override
    public void reload() {
        if (!this.context.isActive()) {
            this.context.refresh();
        }
    }

    @Override
    public AnnotationConfigApplicationContext getContext() {
        return this.context;
    }

    @Override
    public String getName() {
        return context.getId();
    }

    @Override
    public <T> T getService(Class<T> type) {
        return this.context.getBean(type);
    }

    @Override
    public <T> T getService(Class<T> type, String name) {
        return this.context.getBean(name, type);
    }

    @Override
    public <T> T getServiceWith(Class<T> type, Object... arguments) {
        return this.context.getBean(type, arguments);
    }

    @Override
    public <T> T getServiceWith(String name, Object... arguments) {
        return (T) this.context.getBean(name, arguments);
    }

    @Override
    public void addService(Class<?> type) {
        this.context.register(type);
    }

    @Override
    public void addService(ServiceDescriptor descriptor) {
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
                throw new AssertionError(scope.name());

        }

        this.context.registerBeanDefinition(descriptor.getName(), bean);
    }

    @Override
    public void addModule(Class<?> type) {
        this.context.register(type);
    }

}
