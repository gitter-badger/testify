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
import static com.google.common.base.Preconditions.checkState;
import static java.lang.reflect.Modifier.isFinal;
import java.util.Collection;
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
        String mockito = "org.mockito.Mockito";

        try {
            Class.forName(mockito);
        } catch (ClassNotFoundException ex) {
            checkState(false,
                    "'%s' not found in the classpath. Please add mockito to the "
                    + "classpath.",
                    mockito);
        }
    }

    @Override
    public void configuration() {
        String testClassName = context.getTestClassName();
        CutDescriptor cutDescriptor = context.getCutDescriptor();
        Collection<FieldDescriptor> fieldDescriptors = context.getFieldDescriptors().values();

        if (cutDescriptor == null) {
            checkState(!fieldDescriptors.isEmpty(),
                    "Test class '%s' does not define a field annotated with @Cut "
                    + "nor does it have fields annotated with @Real or @Inject. "
                    + "Please insure the test class declares one field annotated "
                    + "with @Cut or declares fields annotated with @Real or @Real.",
                    testClassName
            );
        }

        fieldDescriptors.parallelStream().forEach(p -> {
            Class<?> fieldType = p.getType();
            String fieldName = p.getName();
            String fieldTypeName = p.getTypeName();

            checkState(!isFinal(fieldType.getModifiers()),
                    "Field '%s' in test class '%s' can not be mocked because '%s'"
                    + " is a final class.",
                    fieldName, testClassName, fieldTypeName);

        });
    }

    @Override
    public void wiring() {
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
                    logger.warn("Improper wiring in detected. Class under test '{}' defined "
                            + "in '{}' declars constructor argument of type '{}' but '{}' "
                            + "does not define a field of type '{}' annotated with @Mock.",
                            cutClassName, testClassName, paramTypeName, testClassName, paramTypeName);
                }

            });
        }
    }

}
