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

import static com.google.common.base.Preconditions.checkState;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestException;
import com.fitbur.testify.descriptor.CutDescriptor;
import com.fitbur.testify.descriptor.ParameterDescriptor;
import static java.lang.Class.forName;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
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
        if (CONSTRUCTOR_NAME.equals(name) && constCount == 0) {
            try {
                constCount++;
                Type type = getMethodType(desc);
                Class[] parameterTypes = of(type.getArgumentTypes())
                        .sequential()
                        .map(Type::getClassName)
                        .map(this::getClass)
                        .toArray(Class[]::new);
                CutDescriptor cutDescriptor = context.getCutDescriptor();
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
                throw new TestException(e);
            }
        }

        return null;
    }

    private Class<?> getClass(String p) throws TestException {
        try {
            return forName(p);
        } catch (ClassNotFoundException e) {
            throw new TestException(e);

        }
    }

    @Override
    public void visitEnd() {
        checkState(constCount <= 1,
                "Class under test '%s' has more than one constructor. Please "
                + "insure that the class under test has default constructor or "
                + "only one constructor.",
                context.getCutDescriptor().getField());

    }
}
