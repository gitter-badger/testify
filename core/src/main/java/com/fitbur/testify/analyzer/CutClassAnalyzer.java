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

import com.fitbur.testify.TestContext;
import com.fitbur.testify.descriptor.CutDescriptor;
import com.fitbur.testify.descriptor.ParameterDescriptor;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Class.forName;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import static java.util.stream.Stream.of;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.ASM5;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.getMethodType;

/**
 * A class visitor implementation that performs analysis on the class under
 * test.
 *
 * @author saden
 */
public class CutClassAnalyzer extends ClassVisitor {

    public static final String CONSTRUCTOR_NAME = "<init>";

    private final TestContext context;
    private int constCount = 0;

    public CutClassAnalyzer(TestContext context) {
        super(ASM5);
        this.context = context;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (CONSTRUCTOR_NAME.equals(name)) {
            constCount++;

            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                Type type = getMethodType(desc);
                Class[] parameterTypes = of(type.getArgumentTypes())
                        .sequential()
                        .map(this::getClass)
                        .toArray(Class[]::new);

                CutDescriptor cutDescriptor = context.getCutDescriptor();

                try {
                    Constructor<?> constructor = cutDescriptor
                            .getField()
                            .getType()
                            .getDeclaredConstructor(parameterTypes);

                    cutDescriptor.setConstructor(constructor);

                    Parameter[] parameters = constructor.getParameters();
                    for (int i = 0; i < parameters.length; i++) {
                        ParameterDescriptor descriptor = new ParameterDescriptor(parameters[i], i);
                        context.addParameterDescriptor(descriptor);
                    }

                } catch (NoSuchMethodException | SecurityException e) {
                    checkState(false,
                            "Constructor with '%s' parameters not accessible in '%s' class.",
                            Arrays.toString(parameterTypes), cutDescriptor.getTypeName());
                }
                return null;
            });
        }

        return null;
    }

    @Override
    public void visitEnd() {
        context.setConstructorCount(constCount);
    }

    private Class<?> getClass(Type type) {
        try {
            return forName(type.getInternalName().replace('/', '.'));
        } catch (ClassNotFoundException e) {
            checkState(false, "Class '%s' not found in the classpath.", type.getClassName());
            //not reachable;
            throw new IllegalStateException(e);
        }
    }

}
