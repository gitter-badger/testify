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

import com.fitbur.testify.Cut;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;

/**
 * A small context class that contains metadata about cut class.
 *
 * @author saden
 */
public class CutDescriptor {

    private final Field field;
    private Constructor<?> constructor;
    private Object[] arguments;
    private Optional<Object> instance = empty();

    public CutDescriptor(Field field) {
        this.field = field;
    }

    public Field getField() {
        return field;
    }

    public String getName() {
        return field.getName();
    }

    public Class<?> getType() {
        return field.getType();
    }

    public Type getGenericType() {
        return field.getGenericType();
    }

    public String getTypeName() {
        return field.getType().getSimpleName();
    }

    public Optional<Cut> getCut() {
        return ofNullable(field.getDeclaredAnnotation(Cut.class));
    }

    public boolean hasCut() {
        return field.getDeclaredAnnotation(Cut.class) != null;
    }

    public Optional<Object> getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = ofNullable(instance);
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public <T extends Annotation> Optional<T> getAnnotation(Class<T> type) {
        return of(field.getDeclaredAnnotation(type))
                .filter(p -> p.annotationType().equals(type))
                .map(p -> (T) p)
                .findFirst();
    }

    public Set<? extends Annotation> getAnnotations() {
        return of(field.getDeclaredAnnotations()).collect(toSet());
    }

    public <T extends Annotation> Set<T> getAnnotations(Class<T> type) {
        return of(field.getDeclaredAnnotations())
                .filter(p -> p.annotationType().equals(type))
                .map(p -> (T) p)
                .collect(toSet());
    }

    public <T extends Annotation> boolean hasAnnotation(Class<T> type) {
        return field.getDeclaredAnnotation(type) != null;
    }

    public boolean hasAnyAnnotation(Class<? extends Annotation>... type) {
        return of(type)
                .parallel()
                .distinct()
                .anyMatch(p -> field.getDeclaredAnnotation(p) != null);
    }

    public boolean hasAnnotations(Collection<Class<? extends Annotation>> annotations) {
        return of(field.getDeclaredAnnotations())
                .parallel()
                .distinct()
                .anyMatch(p -> annotations.contains(p.annotationType()));
    }

    public void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.field);
        hash = 53 * hash + Objects.hashCode(this.instance);
        hash = 53 * hash + Objects.hashCode(this.constructor);
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
        final CutDescriptor other = (CutDescriptor) obj;
        if (!Objects.equals(this.field, other.field)) {
            return false;
        }
        if (!Objects.equals(this.instance, other.instance)) {
            return false;
        }
        return Objects.equals(this.constructor, other.constructor);
    }

    @Override
    public String toString() {
        return "CutDescriptor{" + "field=" + field + ", instance=" + instance + ", constructor=" + constructor + '}';
    }

}
