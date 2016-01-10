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
package com.fitbur.testify.system;

import static com.fitbur.guava.common.base.Preconditions.checkState;
import com.fitbur.testify.Fake;
import com.fitbur.testify.Real;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestVerifier;
import com.fitbur.testify.descriptor.CutDescriptor;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.descriptor.ParameterDescriptor;
import com.fitbur.testify.need.Need;
import static java.lang.Class.forName;
import java.lang.reflect.Field;
import static java.lang.reflect.Modifier.isFinal;
import static java.security.AccessController.doPrivileged;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;

/**
 * An integration test verifier that inspects the test context descriptors to
 * make sure everything is properly configured before test class injection is
 * performed and everything is wired correctly after test class injection is
 * performed.
 *
 * @author saden
 */
public class SystemTestVerifier implements TestVerifier {

    private final TestContext testContext;
    private final Logger logger;

    public SystemTestVerifier(TestContext context, Logger logger) {
        this.testContext = context;
        this.logger = logger;
    }

    @Override
    public void dependency() {
        doPrivileged((PrivilegedAction<Object>) () -> {
            Map<String, String> dependencies = new HashMap<String, String>() {
                {
                    put("org.mockito.Mockito", "Mockito");
                    put("org.springframework.context.annotation.AnnotationConfigApplicationContext", "Spring Context");
                }
            };

            dependencies.entrySet().parallelStream().forEach(p -> {
                try {
                    forName(p.getKey());
                } catch (ClassNotFoundException e) {
                    checkState(false,
                            "'%s' not found. Please insure '%s' is in the classpath.",
                            p.getKey(), p.getValue());
                }
            });
            return null;
        });

    }

    @Override
    public void configuration() {
        doPrivileged((PrivilegedAction<Object>) () -> {
            String testClassName = testContext.getTestClassName();
            CutDescriptor cutDescriptor = testContext.getCutDescriptor();
            Collection<FieldDescriptor> fieldDescriptors = testContext.getFieldDescriptors().values();

            if (testContext.getCutCount() == 1) {
                checkState(testContext.getConstructorCount() == 1,
                        "Class under test '%s' defined in test class '%s' has %s constructors. "
                        + "Please insure that the class under test has one and only one constructor.",
                        cutDescriptor.getTypeName(), testClassName, testContext.getConstructorCount());
            }
            // insure that only one field has Cut annotation on it.
            if (testContext.getCutCount() > 1) {
                checkState(false,
                        "Found more than one class under test in %s. Please insure "
                        + "that only one field is annotated with @Cut.",
                        testClassName);
            }

            //insure that there is a field annotated with @Cut defined or one or more
            //fields annotated with @Real or @Inject
            if (testContext.getCutCount() == 0 && fieldDescriptors.isEmpty()) {
                checkState(false,
                        "Test class '%s' does not define a field annotated with @Cut "
                        + "nor does it define field(s) annotated with @Real or @Inject. "
                        + "Please insure the test class defines a single field annotated "
                        + "with @Cut or defines at least one field annotated with @Real "
                        + "or @Inject.",
                        testClassName
                );
            }

            //insure need providers have default constructors.
            testContext.getAnnotations(Need.class).parallelStream().map(Need::value).forEach(p -> {
                try {
                    p.getDeclaredConstructor();
                } catch (NoSuchMethodException e) {
                    checkState(false,
                            "Need provider '%s' defined in test class '%s' does not have a "
                            + "zero argument default constructor. Please insure that the need "
                            + "provider defines an accessible zero argument default constructor.",
                            testClassName, p.getSimpleName()
                    );
                }
            });

            fieldDescriptors.parallelStream().forEach(p -> {
                Field field = p.getField();
                Class<?> fieldType = p.getType();
                String fieldName = p.getName();
                String fieldTypeName = p.getTypeName();

                checkState(!fieldType.isArray(),
                        "Field '%s' in test class '%s' can not be configured because '%s'"
                        + " is an array. Please consider using a List instead of arrays.",
                        fieldName, testClassName, fieldTypeName);
                Fake fake = field.getDeclaredAnnotation(Fake.class);
                if (fake != null) {
                    checkState(!isFinal(fieldType.getModifiers()),
                            "Can not fake field '%s' in test class '%s' because '%s'"
                            + " is a final class.",
                            fieldName, testClassName, fieldTypeName);
                }

                Real real = field.getDeclaredAnnotation(Real.class);
                if (real != null && real.value()) {
                    checkState(!isFinal(fieldType.getModifiers()),
                            "Can not create delegated fake of field '%s' in test class '%s' "
                            + "because '%s' is a final class.",
                            fieldName, testClassName, fieldTypeName);
                }

            });

            return null;
        });
    }

    @Override
    public void wiring() {
        doPrivileged((PrivilegedAction<Object>) () -> {
            CutDescriptor cutDescriptor = testContext.getCutDescriptor();
            String testClassName = testContext.getTestClassName();
            Collection<ParameterDescriptor> paramDescriptors
                    = testContext.getParamaterDescriptors().values();

            if (cutDescriptor != null) {
                String cutClassName = cutDescriptor.getTypeName();
                paramDescriptors.parallelStream().forEach(p -> {
                    Optional instance = p.getInstance();
                    if (!instance.isPresent()) {
                        String paramTypeName = p.getTypeName();
                        logger.warn("Class under test '{}' defined in '{}' has a collaborator "
                                + "of type '{}' but test class '{}' does not define a field of "
                                + "type '{}' annotated with @Fake, @Real, or @Inject. The real "
                                + "instance of the collaborator will be used.",
                                cutClassName, testClassName, paramTypeName, testClassName, paramTypeName);
                    }

                });
            }

            return null;
        });
    }

}
