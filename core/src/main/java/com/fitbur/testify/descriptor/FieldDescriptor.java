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
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;

/**
 * A small context class that contains metadata about test class fields.
 *
 * @author saden
 */
public class FieldDescriptor {

    private final Field field;
    private final Integer order;
    private Optional<Object> instance = empty();

    public FieldDescriptor(Field field, Integer order) {
        this.field = field;
        this.order = order;
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

    public Integer getOrder() {
        return order;
    }

    public Optional<Object> getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = ofNullable(instance);
    }

    public <T extends Annotation> Optional<T> getAnnotation(Class<T> type) {
        return ofNullable(field.getDeclaredAnnotation(type));
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
        return field.isAnnotationPresent(type);
    }

    public boolean hasAnyAnnotation(Class<? extends Annotation>... type) {
        return of(type).anyMatch(field::isAnnotationPresent);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.field);
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
        final FieldDescriptor other = (FieldDescriptor) obj;
        if (!Objects.equals(this.field, other.field)) {
            return false;
        }
        if (!Objects.equals(this.order, other.order)) {
            return false;
        }
        return Objects.equals(this.instance, other.instance);
    }

    @Override
    public String toString() {
        return "FieldDescriptor{" + "field=" + field
                + ", order=" + order
                + ", instance=" + instance
                + '}';
    }

}
