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
package com.fitbur.testify.di;

import java.util.Arrays;
import java.util.Objects;

/**
 * A ServiceDescriptor is a bean-like structure that describes a service
 * declaration.
 *
 * @author saden
 */
public class ServiceDescriptor {

    private final Class<?> type;
    private final String name;
    private final Object[] arguments;
    private final ServiceScope scope;
    private final boolean lazy;
    private final boolean injectable;
    private final boolean primary;

    ServiceDescriptor(Class<?> type,
            String name,
            Object[] arguments,
            ServiceScope scope,
            boolean lazy,
            boolean injectable,
            boolean primary) {
        this.type = type;
        this.name = name;
        this.arguments = arguments;
        this.scope = scope;
        this.lazy = lazy;
        this.injectable = injectable;
        this.primary = primary;
    }

    public <T> Class<T> getType() {
        return (Class<T>) type;
    }

    public String getName() {
        return name;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public ServiceScope getScope() {
        return scope;
    }

    public boolean getLazy() {
        return lazy;
    }

    public boolean getInjectable() {
        return injectable;
    }

    public boolean getPrimary() {
        return primary;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.type);
        hash = 19 * hash + Objects.hashCode(this.name);
        hash = 19 * hash + Arrays.deepHashCode(this.arguments);
        hash = 19 * hash + Objects.hashCode(this.scope);
        hash = 19 * hash + Objects.hashCode(this.lazy);
        hash = 19 * hash + Objects.hashCode(this.injectable);
        hash = 19 * hash + Objects.hashCode(this.primary);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ServiceDescriptor other = (ServiceDescriptor) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Arrays.deepEquals(this.arguments, other.arguments)) {
            return false;
        }
        if (this.scope != other.scope) {
            return false;
        }
        if (!Objects.equals(this.lazy, other.lazy)) {
            return false;
        }
        if (!Objects.equals(this.injectable, other.injectable)) {
            return false;
        }
        return Objects.equals(this.primary, other.primary);
    }

    @Override
    public String toString() {
        return "ServiceDescriptor{" + "type=" + type + ", name=" + name
                + ", arguments=" + Arrays.toString(arguments) + ", scope=" + scope
                + ", lazy=" + lazy + ", injectable=" + injectable
                + ", primary=" + primary + '}';
    }

}
