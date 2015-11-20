/*
 * Copyright 2015 Sharmarke Aden.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import com.fitbur.testify.TestException;
import com.fitbur.testify.descriptor.CutDescriptor;
import com.fitbur.testify.descriptor.DescriptorKey;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.descriptor.MethodDescriptor;
import static java.lang.Class.forName;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static java.util.stream.Stream.of;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.ASM5;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.getMethodType;

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
    private int mockCount = 0;
    private int methodCount = 0;

    public TestClassAnalyzer(TestContext context) {
        super(ASM5);
        this.context = context;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        try {
            Field field = context.getTestClass().getDeclaredField(name);
            Cut cut = field.getDeclaredAnnotation(Cut.class);

            if (cut != null) {
                context.setCutField(new CutDescriptor(field));
                cutCount++;
            } else {
                DescriptorKey key = new DescriptorKey(field.getGenericType(), field.getName());
                FieldDescriptor descriptor = new FieldDescriptor(field, fieldOrder++);
                context.putFieldDescriptor(key, descriptor);
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (CONSTRUCTOR_NAME.equals(name)) {
            return null;
        }

        try {
            Type type = getMethodType(desc);
            Class[] parameterTypes = of(type.getArgumentTypes())
                    .sequential()
                    .map(Type::getClassName)
                    .map(this::getClass)
                    .toArray(Class[]::new);

            Method method = context.getTestClass().getDeclaredMethod(name, parameterTypes);
            MethodDescriptor methodDescriptor = new MethodDescriptor(method, methodOrder++);
            context.addMethodDescriptor(methodDescriptor);

        } catch (NoSuchMethodException | SecurityException e) {
            throw new TestException(e);
        }

        return null;
    }

    @Override
    public void visitEnd() {
        context.setFieldCount(fieldOrder);
        context.setMethodCount(methodOrder);
        context.setCutCount(cutCount);
//        checkState(cutCount == 1,
//                "Class under test not defined or multiple class under test in "
//                + "'%s'. Please insure that a single field on the test "
//                + "class is annotated with @Cut.",
//                context.getTestClass().getName());

    }

    private Class<?> getClass(String p) throws TestException {
        try {
            return forName(p);
        } catch (ClassNotFoundException e) {
            throw new TestException(e);

        }
    }

}
