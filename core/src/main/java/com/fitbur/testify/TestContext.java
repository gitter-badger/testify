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
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import java.util.Set;

/**
 * A small context class that contains metadata about the class under test.
 *
 * @author saden
 */
public class TestContext {

    private final String name;
    private final Class<?> testClass;
    private final Object testInstance;
    private final Map<DescriptorKey, FieldDescriptor> fieldDescriptors = new LinkedHashMap<>();
    private final Set<MethodDescriptor> methodDescriptors = new LinkedHashSet<>();
    private final Map<DescriptorKey, ParameterDescriptor> paramaterDescriptors = new LinkedHashMap<>();
    private CutDescriptor cutDescriptor;
    private int cutCount;
    private int methodCount;
    private int fieldCount;

    public TestContext(String name, Class<?> testClass, Object testInstance) {
        this.name = name;
        this.testClass = testClass;
        this.testInstance = testInstance;
    }

    public String getName() {
        return name;
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public Object getTestInstance() {
        return testInstance;
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

    public void setCutField(CutDescriptor descriptor) {
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

}
