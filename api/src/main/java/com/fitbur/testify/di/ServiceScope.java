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
 * An enumeration of service scopes.
 *
 * @author saden
 */
public enum ServiceScope {

    /**
     * Indicates that a service should be created every time.
     */
    PROTOTYPE,
    /**
     * Indicates that a service should be created once per application context.
     */
    SINGLETON,
    /**
     * Indicates a service that is created once per HTTP request.
     */
    REQUEST,
    /**
     * Indicates that a service should be created once per thread.
     */
    THEAD,
    /**
     * Indicates that a service should be created once per HTTP session.
     */
    SESSION,
    /**
     * Indicates that a service should created be once per servlet context.
     */
    APPLICATION

}
