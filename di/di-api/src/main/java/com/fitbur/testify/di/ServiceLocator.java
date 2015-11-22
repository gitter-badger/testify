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

/**
 * A contract that defines methods for working with various dependency injection
 * frameworks to add services and modules as well as retrieve services.
 *
 * @author saden
 */
public interface ServiceLocator {

    void init();

    void destroy();

    default void reload() {
    }

    <T> T getContext();

    String getName();

    <T> T getService(Class<T> type);

    <T> T getService(Class<T> type, String name);

    <T> T getServiceWith(Class<T> type, Object... arguments);

    <T> T getServiceWith(String name, Object... arguments);

    void addService(Class<?> type);

    void addService(ServiceDescriptor descriptor);

    void addModule(Class<?> type);

}
