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
package com.fitbur.testify.unit;

import com.fitbur.testify.Real;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestVerifier;
import com.fitbur.testify.descriptor.CutDescriptor;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.descriptor.ParameterDescriptor;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.reflect.Modifier.isFinal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import org.slf4j.Logger;

/**
 * A unit test verifier that inspects unit test context descriptors to make sure
 * everything is properly configured before test class injection is performed
 * and everything is wired correctly after test class injection is performed.
 *
 * @author saden
 */
public class UnitTestVerifier implements TestVerifier {

    private final TestContext context;
    private final Logger logger;

    public UnitTestVerifier(TestContext context, Logger logger) {
        this.context = context;
        this.logger = logger;
    }

    @Override
    public void dependency() {
        Map<String, String> dependencies = new HashMap<String, String>() {
            {
                put("org.mockito.Mockito", "Mockito");
            }
        };

        dependencies.entrySet().parallelStream().forEach(p -> {
            try {
                Class.forName(p.getKey());
            } catch (ClassNotFoundException e) {
                checkState(false,
                        "'%s' not found. Please insure '%s' dependency is in the classpath.",
                        p.getKey(), p.getValue());
            }
        });
    }

    @Override
    public void configuration() {
        String testClassName = context.getTestClassName();
        Collection<FieldDescriptor> fieldDescriptors = context.getFieldDescriptors().values();
        CutDescriptor cutDescriptor = context.getCutDescriptor();

        checkState(context.getConstructorCount() == 1,
                "Class under test '%s' has '%d' constructors. Please insure that "
                + "the class under test has one and only one constructor.",
                cutDescriptor.getTypeName(), context.getConstructorCount());

        fieldDescriptors.parallelStream().forEach(p -> {

            Class<?> fieldType = p.getType();
            String fieldName = p.getName();
            String fieldTypeName = p.getTypeName();

            checkState(!isFinal(fieldType.getModifiers()),
                    "Field '%s' in test class '%s' can not be mocked because '%s'"
                    + " is a final class.",
                    fieldName, testClassName, fieldTypeName);

            checkState(!p.hasAnyAnnotation(Real.class, Inject.class),
                    "Field '%s' in test class '%s' is not annotated with @Mock. "
                    + "@Real and @Inject annotations not supported for "
                    + "unit tests. Please use @Mock instead.",
                    fieldName, fieldTypeName
            );
        });
    }

    @Override
    public void wiring() {
        CutDescriptor cutDescriptor = context.getCutDescriptor();
        String testClassName = context.getTestClassName();
        String cutClassName = cutDescriptor.getTypeName();
        Collection<ParameterDescriptor> fieldDescriptors
                = context.getParamaterDescriptors().values();

        fieldDescriptors.parallelStream().forEach(p -> {
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

}
