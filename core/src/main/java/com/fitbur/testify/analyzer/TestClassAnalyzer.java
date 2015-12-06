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
package com.fitbur.testify.analyzer;

import com.fitbur.testify.Cut;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.descriptor.CutDescriptor;
import com.fitbur.testify.descriptor.DescriptorKey;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.descriptor.MethodDescriptor;
import com.fitbur.testify.need.Need;
import com.fitbur.testify.need.Needs;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Class.forName;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import static java.util.stream.Stream.of;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.ASM5;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.getMethodType;
import static org.objectweb.asm.Type.getType;

/**
 * A class visitor implementation that performs analysis on the test class.
 *
 * @author saden
 */
public class TestClassAnalyzer extends ClassVisitor {

    public static final String CONSTRUCTOR_NAME = "<init>";
    private final TestContext context;
    private int fieldOrder = 0;
    private int methodOrder = 0;
    private int cutCount = 0;

    public TestClassAnalyzer(TestContext context) {
        super(ASM5);
        this.context = context;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        Type type = getType(desc);
        String typeClassName = type.getClassName();
        Class<?> testClass = context.getTestClass();

        if (typeClassName.equals(Need.class.getName())) {
            Need need = testClass.getDeclaredAnnotation(Need.class);
            context.addNeed(need);
        }

        if (type.getClassName().equals(Needs.class.getName())) {
            Needs needs = testClass.getDeclaredAnnotation(Needs.class);
            for (Need needVal : needs.value()) {
                context.addNeed(needVal);
            }
        }

        return null;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        try {
            Class<?> testClass = context.getTestClass();
            Field field = testClass.getDeclaredField(name);
            Cut cut = field.getDeclaredAnnotation(Cut.class);

            if (cut != null) {
                context.setCutDescriptor(new CutDescriptor(field));
                cutCount++;
            } else {
                DescriptorKey key = new DescriptorKey(field.getGenericType(), field.getName());
                FieldDescriptor descriptor = new FieldDescriptor(field, fieldOrder++);
                context.putFieldDescriptor(key, descriptor);
            }
        } catch (NoSuchFieldException e) {
            checkState(false,
                    "Field '%s' not found in test class '%s'.\n%s",
                    name, context.getTestClassName(), e.getMessage());

            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (CONSTRUCTOR_NAME.equals(name)) {
            return null;
        }
        Class<?> testClass = context.getTestClass();

        Type type = getMethodType(desc);
        Class[] parameterTypes = of(type.getArgumentTypes())
                .sequential()
                .map(Type::getClassName)
                .map(this::getClass)
                .toArray(Class[]::new);

        try {

            Method method = testClass.getDeclaredMethod(name, parameterTypes);
            MethodDescriptor methodDescriptor
                    = new MethodDescriptor(method, parameterTypes, methodOrder++);
            context.addMethodDescriptor(methodDescriptor);

        } catch (NoSuchMethodException | SecurityException e) {
            checkState(false,
                    "Method with '%s' parameters not accessible in '%s' class.",
                    Arrays.toString(parameterTypes), context.getTestClassName());
        }

        return null;
    }

    @Override
    public void visitEnd() {
        context.setFieldCount(fieldOrder);
        context.setMethodCount(methodOrder);
        context.setCutCount(cutCount);
    }

    private Class<?> getClass(String className) {
        try {
            return forName(className);
        } catch (ClassNotFoundException e) {
            checkState(false, "Class '%s' not found in the classpath.", className);
            //not reachable;
            throw new IllegalStateException(e);
        }
    }

}
