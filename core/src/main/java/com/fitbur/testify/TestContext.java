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

import com.fitbur.testify.descriptor.CutDescriptor;
import com.fitbur.testify.descriptor.DescriptorKey;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.descriptor.MethodDescriptor;
import com.fitbur.testify.descriptor.ParameterDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collection;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
import org.slf4j.Logger;

/**
 * A small context class that contains metadata about the class under test.
 *
 * @author saden
 */
public class TestContext {

    private final String name;
    private final Class<?> testClass;
    private final Map<DescriptorKey, FieldDescriptor> fieldDescriptors = new LinkedHashMap<>();
    private final Set<MethodDescriptor> methodDescriptors = new LinkedHashSet<>();
    private final Map<DescriptorKey, ParameterDescriptor> paramaterDescriptors = new LinkedHashMap<>();

    private Object testInstance;
    private CutDescriptor cutDescriptor;
    private int cutCount;
    private int methodCount;
    private int fieldCount;
    private final Logger logger;
    private int constructorCount;

    public TestContext(String name, Class<?> testClass, Logger logger) {
        this.name = name;
        this.testClass = testClass;
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }

    public String getName() {
        return name;
    }

    public String getTestClassName() {
        return testClass.getSimpleName();
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public Object getTestInstance() {
        return testInstance;
    }

    public void setTestInstance(Object testInstance) {
        this.testInstance = testInstance;
    }

    public void addParameterDescriptor(ParameterDescriptor descriptor) {
        Parameter parameter = descriptor.getParameter();
        Type paramType = parameter.getParameterizedType();
        String paramName = parameter.getName();

        paramaterDescriptors.put(new DescriptorKey(paramType, paramName), descriptor);
    }

    public Map<DescriptorKey, ParameterDescriptor> getParamaterDescriptors() {
        return unmodifiableMap(paramaterDescriptors);
    }

    public void putFieldDescriptor(DescriptorKey key, FieldDescriptor value) {
        fieldDescriptors.put(key, value);
    }

    public Optional<FieldDescriptor> getFieldDescriptor(DescriptorKey key) {
        return ofNullable(fieldDescriptors.get(key));
    }

    public Map<DescriptorKey, FieldDescriptor> getFieldDescriptors() {
        return unmodifiableMap(fieldDescriptors);
    }

    public void addMethodDescriptor(MethodDescriptor value) {
        methodDescriptors.add(value);
    }

    public Set<MethodDescriptor> getMethodDescriptors() {
        return unmodifiableSet(methodDescriptors);
    }

    public Set<MethodDescriptor> getConfigMethods() {
        return methodDescriptors.stream()
                .filter(p -> p.hasAnnotation(Config.class))
                .collect(toSet());
    }

    public Optional<MethodDescriptor> getConfigMethod(Class... parameterTypes) {
        return methodDescriptors.stream()
                .filter(p -> p.hasAnnotation(Config.class))
                .filter(p -> p.hasParameterTypes(parameterTypes))
                .findFirst();
    }

    public void setCutDescriptor(CutDescriptor descriptor) {
        this.cutDescriptor = descriptor;
    }

    public CutDescriptor getCutDescriptor() {
        return cutDescriptor;
    }

    public int getCutCount() {
        return cutCount;
    }

    public void setCutCount(int cutCount) {
        this.cutCount = cutCount;
    }

    public int getMethodCount() {
        return methodCount;
    }

    public void setMethodCount(int methodCount) {
        this.methodCount = methodCount;
    }

    public int getFieldCount() {
        return fieldCount;
    }

    public void setFieldCount(int fieldCount) {
        this.fieldCount = fieldCount;
    }

    public void setConstructorCount(int constructorCount) {
        this.constructorCount = constructorCount;
    }

    public int getConstructorCount() {
        return constructorCount;
    }

    public Set<? extends Annotation> getAnnotations() {
        return of(testClass.getDeclaredAnnotations()).collect(toSet());
    }

    public <T extends Annotation> Optional<T> getAnnotation(Class<T> annotationType) {
        return ofNullable(testClass.getDeclaredAnnotation(annotationType));
    }

    public <T extends Annotation> Set<T> getAnnotations(Class<T> annotationType) {
        return of(testClass.getDeclaredAnnotationsByType(annotationType)).collect(toSet());
    }

    public <T extends Annotation> boolean hasAnnotation(Class<T> type) {
        return testClass.getDeclaredAnnotation(type) != null;
    }

    public boolean hasAnyAnnotation(Class<? extends Annotation>... type) {
        return of(type)
                .parallel()
                .distinct()
                .anyMatch(p -> testClass.getDeclaredAnnotation(p) != null);
    }

    public boolean hasAnnotations(Collection<Class<? extends Annotation>> annotations) {
        return of(testClass.getDeclaredAnnotations())
                .parallel()
                .distinct()
                .anyMatch(p -> annotations.contains(p.annotationType()));
    }

}
