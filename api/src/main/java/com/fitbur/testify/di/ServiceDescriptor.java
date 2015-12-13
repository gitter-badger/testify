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
    private final ServiceScope scope;
    private Object[] arguments;
    private boolean lazy;
    private boolean injectable;
    private boolean discoverable;
    private boolean primary;

    ServiceDescriptor(Class<?> type, String name, ServiceScope scope) {
        this.type = type;
        this.name = name;
        this.scope = scope;
    }

    /**
     * Get the type of the service.
     *
     * @param <T> the type of the service
     * @return get the service class
     */
    public <T> Class<T> getType() {
        return (Class<T>) type;
    }

    /**
     * Get the name of the service.
     *
     * @return the service name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the constructor arguments used to instantiate the service.
     *
     * @return an array constructor arguments instances
     */
    public Object[] getArguments() {
        return arguments;
    }

    /**
     * Set the service constructor arguments.
     *
     * @param arguments an array of objects.
     */
    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    /**
     * Get the scope of the service.
     *
     * @return the scope of the service
     */
    public ServiceScope getScope() {
        return scope;
    }

    /**
     * Determine whether the service is lazy-loaded service.
     *
     * @return true if the service is lazy-loaded, false otherwise
     */
    public boolean getLazy() {
        return lazy;
    }

    /**
     * Set whether the service is lazy loaded.
     *
     * @param lazy set true if the service is lazy-loaded, false otherwise
     */
    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    /**
     * Determine whether the service can be injected with collaborators.
     *
     * @return true if the service is an injectable service, false otherwise
     */
    public boolean getInjectable() {
        return injectable;
    }

    /**
     * Set whether the service can be injected with collaborators.
     *
     * @param injectable set true for injectable services, false otherwise
     */
    public void setInjectable(boolean injectable) {
        this.injectable = injectable;
    }

    /**
     * Determine whether the service is discoverable and available for wiring.
     *
     * @return true if the service is an injectable service, false otherwise
     */
    public boolean getDiscoverable() {
        return discoverable;
    }

    /**
     * Set whether the service can be discovered.
     *
     * @param discoverable set true if discoverable, false otherwise
     */
    public void setDiscoverable(boolean discoverable) {
        this.discoverable = discoverable;
    }

    /**
     * Determine whether the service is a primary service.
     *
     * @return true if the service is a primary service, false otherwise
     */
    public boolean getPrimary() {
        return primary;
    }

    /**
     * Set whether the service is a primary service.
     *
     * @param primary set true if a primary service, false otherwise
     */
    public void setPrimary(boolean primary) {
        this.primary = primary;
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
        hash = 19 * hash + Objects.hashCode(this.discoverable);
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
        if (!Objects.equals(this.discoverable, other.discoverable)) {
            return false;
        }
        return Objects.equals(this.primary, other.primary);
    }

    @Override
    public String toString() {
        return "ServiceDescriptor{" + "type=" + type + ", name=" + name
                + ", arguments=" + Arrays.toString(arguments) + ", scope=" + scope
                + ", lazy=" + lazy + ", injectable=" + injectable
                + ", discoverable=" + discoverable + ", primary=" + primary + '}';
    }

}
