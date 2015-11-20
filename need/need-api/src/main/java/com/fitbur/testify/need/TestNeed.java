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
package com.fitbur.testify.need;

import com.fitbur.testify.di.TestServiceLocator;

/**
 * A contract that defines methods must be implement by a test need so the
 * life-cycle of the test need can be managed.
 *
 * @author saden
 * @param <T> the type of the context associated with need implementation
 */
public interface TestNeed<T> {

    T init(Object testInstance, TestServiceLocator serviceLocator);

    default void config(T context) {
    }

    default void destroy(T context) {

    }

    default void inject(T context) {
    }

    default void before(T context) {
    }

    default void after(T context) {
    }

}
