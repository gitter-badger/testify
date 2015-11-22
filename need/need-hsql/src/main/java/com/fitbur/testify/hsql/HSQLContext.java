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
package com.fitbur.testify.hsql;

import com.fitbur.testify.di.ServiceDescriptor;
import com.fitbur.testify.di.ServiceLocator;
import java.util.Objects;

/**
 * The context associated with HSQL database need.
 *
 * @author saden
 */
public class HSQLContext {

    private final Object testInstance;

    private final ServiceDescriptor descriptor;
    private final ServiceLocator serviceLocator;

    HSQLContext(Object testInstance,
            ServiceDescriptor descriptor,
            ServiceLocator serviceLocator) {
        this.testInstance = testInstance;
        this.descriptor = descriptor;
        this.serviceLocator = serviceLocator;
    }

    public ServiceDescriptor getDescriptor() {
        return descriptor;
    }

    public Object getTestInstance() {
        return testInstance;
    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.descriptor);
        hash = 53 * hash + Objects.hashCode(this.serviceLocator);
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
        final HSQLContext other = (HSQLContext) obj;
        if (!Objects.equals(this.descriptor, other.descriptor)) {
            return false;
        }
        return Objects.equals(this.serviceLocator, other.serviceLocator);
    }

    @Override
    public String toString() {
        return "HSQLContext{" + "descriptor=" + descriptor + ", serviceLocator=" + serviceLocator + '}';
    }

}
