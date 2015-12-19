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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A class that contains annotations classes supported by the service locator.
 *
 * @author saden
 */
public class ServiceAnnotations {

    Set<Class<? extends Annotation>> injectors = new HashSet<>();
    Set<Class<? extends Annotation>> namedQualifiers = new HashSet<>();
    Set<Class<? extends Annotation>> customQualifiers = new HashSet<>();

    /**
     * Add one or more injector annotation.
     *
     * @param injectors an array of injector annotations.
     */
    public void addInjectors(Class<? extends Annotation>... injectors) {
        this.injectors.addAll(Arrays.asList(injectors));
    }

    /**
     * Add one or more named qualifier annotation.
     *
     * @param namedQualifiers an array of named qualifier annotations.
     */
    public void addNamedQualifier(Class<? extends Annotation>... namedQualifiers) {
        this.namedQualifiers.addAll(Arrays.asList(namedQualifiers));
    }

    /**
     * Add one or more custom qualifier annotation.
     *
     * @param customQualifiers an array of custom qualifier annotations.
     */
    public void addCustomQualfier(Class<? extends Annotation>... customQualifiers) {
        this.customQualifiers.addAll(Arrays.asList(customQualifiers));
    }

    /**
     * Get all the injector annotations classes.
     *
     * @return a set containing injector annotation classes.
     */
    public Set<Class<? extends Annotation>> getInjectors() {
        return injectors;
    }

    /**
     * Get all the named qualifier annotations classes.
     *
     * @return a set containing named qualifier annotation classes.
     */
    public Set<Class<? extends Annotation>> getNamedQualifiers() {
        return namedQualifiers;
    }

    /**
     * Get all the custom qualifier annotations classes.
     *
     * @return a set containing custom qualifier annotation classes.
     */
    public Set<Class<? extends Annotation>> getCustomQualifiers() {
        return customQualifiers;
    }

}
