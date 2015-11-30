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

import java.util.Objects;

/**
 * A small context object that contains need contextual information.
 *
 * @author saden
 */
public class NeedContext {

    private final NeedProvider provider;
    private final NeedDescriptor descriptor;
    private final Object config;

    public NeedContext(NeedProvider provider, NeedDescriptor descriptor, Object config) {
        this.provider = provider;
        this.descriptor = descriptor;
        this.config = config;
    }

    public NeedProvider getProvider() {
        return provider;
    }

    public NeedDescriptor getDescriptor() {
        return descriptor;
    }

    public Object getConfig() {
        return config;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(this.provider);
        hash = 71 * hash + Objects.hashCode(this.descriptor);
        hash = 71 * hash + Objects.hashCode(this.config);
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
        return Objects.equals(this.config, other.config);
    }

    @Override
    public String toString() {
        return "NeedContext{" + "provider=" + provider + ", descriptor=" + descriptor + ", config=" + config + '}';
    }

}
