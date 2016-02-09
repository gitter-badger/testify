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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A small configuration object that contains need contextual information. This
 * configuration is used in conjunction with {@link NeedProvider} to manage the
 * life-cycle of a need.
 *
 * @author saden
 */
public class NeedContext {

    private final NeedProvider provider;
    private final NeedDescriptor descriptor;
    private final Map<String, NeedInstance> instances;
    private final Object configuration;

    public NeedContext(NeedProvider provider,
            NeedDescriptor descriptor,
            Map<String, NeedInstance> instances,
            Object configuration) {
        this.provider = provider;
        this.descriptor = descriptor;
        this.instances = instances;
        this.configuration = configuration;
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
     * Get a map containing need instances.
     *
     * @return a map containing a need instances
     */
    public Map<String, ? extends NeedInstance> getInstances() {
        return instances;
    }

    /**
     * Find the first need instance.
     *
     * @return an optional containing a need instance, empty optional otherwise
     */
    public Optional<NeedInstance> findFirstInstance() {
        return instances.values().stream().findFirst();
    }

    /**
     * Get the need configuration configuration object.
     *
     * @return the need configuration configuration.
     */
    public Object getConfiguration() {
        return configuration;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(this.provider);
        hash = 71 * hash + Objects.hashCode(this.descriptor);
        hash = 71 * hash + Objects.hashCode(this.instances);
        hash = 71 * hash + Objects.hashCode(this.configuration);
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

        return Objects.equals(this.configuration, other.configuration);
    }

    @Override
    public String toString() {
        return "NeedContext{"
                + "provider=" + provider
                + ", descriptor=" + descriptor
                + ", instances=" + instances
                + ", context=" + configuration
                + '}';
    }

}
