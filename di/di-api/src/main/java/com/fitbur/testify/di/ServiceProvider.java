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

import javax.inject.Provider;

/**
 * ServiceProvider is a class that enables the creation of a provider instance
 * that retrieves a service from the service locator.
 *
 * @author saden
 */
public class ServiceProvider implements Provider {

    private final ServiceLocator locator;
    private final Class<?> serviceType;

    public ServiceProvider(ServiceLocator locator, Class<?> serviceType) {
        this.locator = locator;
        this.serviceType = serviceType;
    }

    /**
     * Provides a fully-constructed and injected instance of the service from
     * the service locator.
     *
     * @return an instance of the service.
     */
    @Override
    public Object get() {
        return locator.getService(serviceType);
    }

}
