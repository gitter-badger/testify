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

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
import javax.inject.Qualifier;

/**
 * A small context class that contains metadata about class under test
 * constructor parameters.
 *
 * @author saden
 */
public class ParameterDescriptor {

    private final Parameter parameter;
    private final Integer index;
    private Optional<Object> instance = empty();

    public ParameterDescriptor(Parameter parameter, Integer index) {
        this.parameter = parameter;
        this.index = index;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public String getName() {
        return parameter.getName();
    }

    public Integer getIndex() {
        return index;
    }

    public Class<?> getType() {
        return parameter.getType();
    }

    public Type getGenericType() {
        return parameter.getParameterizedType();
    }

    public String getTypeName() {
        return parameter.getType().getSimpleName();
    }

    public Optional<Object> getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = ofNullable(instance);
    }

    public <T extends Annotation> Optional<T> getAnnotation(Class<T> type) {
        return of(parameter.getDeclaredAnnotation(type))
                .filter(p -> p.annotationType().equals(type))
                .map(p -> (T) p)
                .findFirst();
    }

    public Set<? extends Annotation> getAnnotations() {
        return of(parameter.getDeclaredAnnotations()).parallel()
                .collect(toSet());
    }

    public Set<? extends Annotation> getQualifiers() {
        return of(parameter.getDeclaredAnnotations())
                .filter(p -> p.annotationType().isAnnotationPresent(Qualifier.class))
                .collect(toSet());
    }

    public <T extends Annotation> Set<T> getAnnotations(Class<T> type) {
        return of(parameter.getDeclaredAnnotations())
                .filter(p -> p.annotationType().equals(type))
                .map(p -> (T) p)
                .collect(toSet());
    }

    public <T extends Annotation> boolean hasAnnotation(Class<T> type) {
        return parameter.isAnnotationPresent(type);
    }

    public boolean hasAnyAnnotation(Class<? extends Annotation>... type) {
        return of(type).anyMatch(parameter::isAnnotationPresent);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.parameter);
        hash = 59 * hash + Objects.hashCode(this.index);
        hash = 59 * hash + Objects.hashCode(this.instance);
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
        final ParameterDescriptor other = (ParameterDescriptor) obj;
        if (!Objects.equals(this.parameter, other.parameter)) {
            return false;
        }
        if (!Objects.equals(this.index, other.index)) {
            return false;
        }
        return Objects.equals(this.instance, other.instance);
    }

    @Override
    public String toString() {
        return "ParameterDescriptor{" + "parameter=" + parameter + ", index=" + index + ", instance=" + instance + '}';
    }

}
