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
package com.fitbur.testify.need;

import com.fitbur.testify.di.ServiceLocator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import static java.util.Optional.ofNullable;

/**
 * A small context object that contains need contextual information. This
 * context is used in conjunction with {@link NeedProvider} to manage the
 * life-cycle of a need.
 *
 * @author saden
 */
public class NeedContext {

    private final NeedProvider provider;
    private final NeedDescriptor descriptor;
    private final Map<String, NeedInstance> instances;
    private final ServiceLocator locator;
    private final Object context;

    public NeedContext(NeedProvider provider,
            NeedDescriptor descriptor,
            Map<String, NeedInstance> instances,
            ServiceLocator locator,
            Object context) {
        this.provider = provider;
        this.descriptor = descriptor;
        this.instances = instances;
        this.locator = locator;
        this.context = context;
    }

    /**
     * Get the need provider.
     *
     * @return the need provider
     */
    public NeedProvider getProvider() {
        return provider;
    }

    /**
     * Get the need descriptor.
     *
     * @return the need descriptor.
     */
    public NeedDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Get an optional containing need instances associated with the need.
     *
     * @return an optional containing the need instances, an empty otherwise
     */
    Optional<Map<String, ? extends NeedInstance>> getInstances() {
        return ofNullable(instances);
    }

    /**
     * Get an optional service locator associated with the need.
     *
     * @return an optional containing the service locator, an empty otherwise
     */
    Optional<? extends ServiceLocator> getServiceLocator() {
        return ofNullable(locator);
    }

    /**
     * Get the need configuration context object.
     *
     * @return the need context configuration.
     */
    public Object getContext() {
        return context;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(this.provider);
        hash = 71 * hash + Objects.hashCode(this.descriptor);
        hash = 71 * hash + Objects.hashCode(this.instances);
        hash = 71 * hash + Objects.hashCode(this.locator);
        hash = 71 * hash + Objects.hashCode(this.context);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NeedContext other = (NeedContext) obj;
        if (!Objects.equals(this.provider, other.provider)) {
            return false;
        }
        if (!Objects.equals(this.descriptor, other.descriptor)) {
            return false;
        }
        if (!Objects.equals(this.instances, other.instances)) {
            return false;
        }
        if (!Objects.equals(this.locator, other.locator)) {
            return false;
        }
        return Objects.equals(this.context, other.context);
    }

    @Override
    public String toString() {
        return "NeedContext{"
                + "provider=" + provider
                + ", descriptor=" + descriptor
                + ", instances=" + instances
                + ", locator=" + locator
                + ", context=" + context
                + '}';
    }

}
