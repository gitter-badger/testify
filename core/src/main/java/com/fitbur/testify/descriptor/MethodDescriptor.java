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
package com.fitbur.testify.descriptor;

import com.fitbur.testify.Mock;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import java.util.Set;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;

/**
 * A small context class that contains metadata about test class methods.
 *
 * @author saden
 */
public class MethodDescriptor {

    private final Method method;
    private final Integer order;
    private Object instance;

    public MethodDescriptor(Method method, Integer order) {
        this.method = method;
        this.order = order;
    }

    public Method getMethod() {
        return method;
    }

    public Optional<Mock> getMock() {
        return ofNullable(method.getDeclaredAnnotation(Mock.class));
    }

    public Integer getOrder() {
        return order;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public <T extends Annotation> Optional<T> getAnnotation(Class<T> type) {
        return of(method.getDeclaredAnnotation(type))
                .filter(p -> p.annotationType().equals(type))
                .map(p -> (T) p)
                .findFirst();
    }

    public Set<? extends Annotation> getAnnotations() {
        return of(method.getDeclaredAnnotations()).collect(toSet());
    }

    public <T extends Annotation> List<T> getAnnotations(Class<T> type) {
        return of(method.getDeclaredAnnotations())
                .filter(p -> p.annotationType().equals(type))
                .map(p -> (T) p)
                .collect(toList());
    }

    public <T extends Annotation> boolean hasAnnotation(Class<T> type) {
        return method.isAnnotationPresent(type);
    }

    public boolean hasAnyAnnotation(Class<? extends Annotation>... type) {
        return of(type).anyMatch(method::isAnnotationPresent);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.method);
        hash = 67 * hash + Objects.hashCode(this.order);
        hash = 67 * hash + Objects.hashCode(this.instance);
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
        final MethodDescriptor other = (MethodDescriptor) obj;
        if (!Objects.equals(this.method, other.method)) {
            return false;
        }
        if (!Objects.equals(this.order, other.order)) {
            return false;
        }
        return Objects.equals(this.instance, other.instance);
    }

    @Override
    public String toString() {
        return "MethodDescriptor{"
                + "method=" + method
                + ", order=" + order
                + ", instance=" + instance
                + '}';
    }

}
