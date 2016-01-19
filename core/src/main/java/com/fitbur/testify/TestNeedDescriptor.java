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
package com.fitbur.testify;

import com.fitbur.testify.di.ServiceLocator;
import com.fitbur.testify.need.Need;
import com.fitbur.testify.need.NeedDescriptor;
import java.lang.annotation.Annotation;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;

/**
 * Spring need descriptor.
 *
 * @author saden
 */
public class TestNeedDescriptor implements NeedDescriptor {

    private final Need need;
    private final TestContext testContext;
    private final ServiceLocator serviceLocator;
    private final String methodName;

    public TestNeedDescriptor(Need need,
            TestContext testContext,
            String methodName,
            ServiceLocator serviceLocator) {
        this.need = need;
        this.testContext = testContext;
        this.methodName = methodName;
        this.serviceLocator = serviceLocator;
    }

    @Override
    public Need getNeed() {
        return need;
    }

    @Override
    public Object getTestInstance() {
        return testContext.getTestInstance();
    }

    @Override
    public Class<?> getTestClass() {
        return testContext.getTestClass();
    }

    @Override
    public String getTestClassName() {
        return testContext.getTestClassName();
    }

    @Override
    public String getTestMethodName() {
        return methodName;
    }

    @Override
    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    @Override
    public <T extends Annotation> Optional<T> getAnnotation(Class<T> type) {
        T result = testContext.getTestInstance().getClass().getAnnotation(type);

        return ofNullable(result);
    }

    @Override
    public <T extends Annotation> Set<T> getAnnotations(Class<T> type) {
        T[] result = testContext.getTestInstance().getClass().getAnnotationsByType(type);

        return of(result).collect(toSet());
    }

}
