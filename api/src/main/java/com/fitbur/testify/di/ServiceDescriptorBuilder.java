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
package com.fitbur.testify.di;

/**
 * A class that facilitates the construction of a {@link ServiceDescriptor}.
 *
 * @author saden
 * @see ServiceDescriptor
 */
public class ServiceDescriptorBuilder {

    private Class<?> type;
    private String name;
    private ServiceScope scope = ServiceScope.PROTOTYPE;
    private boolean lazy = true;
    private boolean injectable = true;
    private boolean primary = true;
    private Object[] arguments = new Object[]{};

    public ServiceDescriptorBuilder() {
    }

    public ServiceDescriptorBuilder type(Class<?> type) {
        this.type = type;
        return this;
    }

    public ServiceDescriptorBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ServiceDescriptorBuilder arguments(Object... arguments) {
        this.arguments = arguments;
        return this;
    }

    public ServiceDescriptorBuilder scope(ServiceScope scope) {
        this.scope = scope;
        return this;
    }

    public ServiceDescriptorBuilder lazy(boolean lazy) {
        this.lazy = lazy;
        return this;
    }

    public ServiceDescriptorBuilder injectable(boolean injectable) {
        this.injectable = injectable;
        return this;
    }

    public ServiceDescriptorBuilder primary(boolean primary) {
        this.primary = primary;
        return this;
    }

    /**
     * Build and return an instance of the service descriptor.
     *
     * @return a service descriptor instance.
     */
    public ServiceDescriptor build() {
        return new ServiceDescriptor(type,
                name,
                arguments,
                scope,
                lazy,
                injectable,
                primary);
    }

}
