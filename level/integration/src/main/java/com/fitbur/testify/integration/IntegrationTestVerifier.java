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
package com.fitbur.testify.integration;

import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestVerifier;
import com.fitbur.testify.descriptor.CutDescriptor;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.descriptor.ParameterDescriptor;
import com.fitbur.testify.need.Need;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.reflect.Modifier.isFinal;
import java.security.AccessController;
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
public class IntegrationTestVerifier implements TestVerifier {

    private final TestContext context;
    private final Logger logger;

    public IntegrationTestVerifier(TestContext context, Logger logger) {
        this.context = context;
        this.logger = logger;
    }

    @Override
    public void dependency() {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            Map<String, String> dependencies = new HashMap<String, String>() {
                {
                    put("org.mockito.Mockito", "Mockito");
                    put("org.springframework.context.annotation.AnnotationConfigApplicationContext", "Spring Context");
                }
            };

            dependencies.entrySet().parallelStream().forEach(p -> {
                try {
                    Class.forName(p.getKey());
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
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            String testClassName = context.getTestClassName();
            CutDescriptor cutDescriptor = context.getCutDescriptor();
            Collection<FieldDescriptor> fieldDescriptors = context.getFieldDescriptors().values();

            if (context.getCutCount() == 1) {
                checkState(context.getConstructorCount() == 1,
                        "Class under test '%s' has '%d' constructors. Please insure that "
                        + "the class under test has one and only one constructor.",
                        cutDescriptor.getTypeName(), context.getConstructorCount());
            }
            // insure that only one field has Cut annotation on it.
            if (context.getCutCount() > 1) {
                checkState(false,
                        "Found more than one class under test in %s. Please insure "
                        + "that only one field is annotated with @Cut.",
                        testClassName);
            }

            //insure that there is a field annotated with @Cut defined or one or more
            //fields annotated with @Real or @Inject
            if (context.getCutCount() == 0 && fieldDescriptors.isEmpty()) {
                checkState(false,
                        "Test class '%s' does not define a field annotated with @Cut "
                        + "nor does it have fields annotated with @Real or @Inject. "
                        + "Please insure the test class declares a single field annotated "
                        + "with @Cut or declares fields annotated with @Real or @Real.",
                        testClassName
                );
            }

            //insure need providers have default constructors.
            context.getNeeds().parallelStream().map(Need::value).forEach(p -> {
                try {
                    p.getDeclaredConstructor();
                } catch (NoSuchMethodException e) {
                    checkState(false,
                            "Need provider '%s' defined in test class '%s' does not have a "
                            + "zero argument default constructor. Please insure that the need "
                            + "provider defines accessible zero argument default constructor.",
                            testClassName, p.getSimpleName()
                    );
                }
            });

            fieldDescriptors.parallelStream().forEach(p -> {
                Class<?> fieldType = p.getType();
                String fieldName = p.getName();
                String fieldTypeName = p.getTypeName();

                checkState(!isFinal(fieldType.getModifiers()),
                        "Field '%s' in test class '%s' can not be mocked because '%s'"
                        + " is a final class.",
                        fieldName, testClassName, fieldTypeName);

            });

            return null;
        });
    }

    @Override
    public void wiring() {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            CutDescriptor cutDescriptor = context.getCutDescriptor();
            String testClassName = context.getTestClassName();
            Collection<ParameterDescriptor> paramDescriptors
                    = context.getParamaterDescriptors().values();

            if (cutDescriptor != null) {
                String cutClassName = cutDescriptor.getTypeName();
                paramDescriptors.parallelStream().forEach(p -> {
                    Optional instance = p.getInstance();
                    if (!instance.isPresent()) {
                        String paramTypeName = p.getTypeName();
                        logger.warn("Improper wiring detected. Class under test '{}' defined "
                                + "in '{}' declars constructor argument of type '{}' but '{}' "
                                + "does not define a field of type '{}' annotated with @Mock.",
                                cutClassName, testClassName, paramTypeName, testClassName, paramTypeName);
                    }

                });
            }

            return null;
        });
    }

}
