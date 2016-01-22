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
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;

/**
 * A descriptor class passed to {@link NeedProvider} to instantiate a need
 * instance.
 *
 * @author saden
 */
public interface NeedDescriptor {

    /**
     * Get the need annotation.
     *
     * @return the need annotation.
     */
    Need getNeed();

    /**
     * Get the test instance.
     *
     * @return an instance of the test class
     */
    Object getTestInstance();

    /**
     * Get the test class.
     *
     * @return the test class
     */
    Class<?> getTestClass();

    /**
     * Get the name of the test class name.
     *
     * @return the test class name
     */
    String getTestClassName();

    /**
     * Get the name of the test method.
     *
     * @return the test class name
     */
    String getTestMethodName();

//    /**
//     * Get the need instance.
//     *
//     * @return the need instance
//     */
//    NeedInstance getNeedInstance();

    /**
     * Get the service locator.
     *
     * @return service locator
     */
    ServiceLocator getServiceLocator();

    /**
     * Get annotation of the given type.
     *
     * @param <T> the annotation type
     * @param type the annotation class
     * @return an optional containing annotation, or an empty optional
     */
    <T extends Annotation> Optional<T> getAnnotation(Class<T> type);

    /**
     * Get annotations of the given type.
     *
     * @param <T> the annotation type
     * @param type the annotation class
     * @return a set of annotations, or an empty set
     */
    <T extends Annotation> Set<T> getAnnotations(Class<T> type);
}
