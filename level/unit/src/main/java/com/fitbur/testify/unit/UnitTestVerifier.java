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

import static com.fitbur.guava.common.base.Preconditions.checkState;
import com.fitbur.testify.Real;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestVerifier;
import com.fitbur.testify.descriptor.CutDescriptor;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.descriptor.ParameterDescriptor;
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

    private final TestContext testContext;
    private final Logger logger;

    public UnitTestVerifier(TestContext testContext, Logger logger) {
        this.testContext = testContext;
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
        String testClassName = testContext.getTestClassName();
        Collection<FieldDescriptor> fieldDescriptors = testContext.getFieldDescriptors().values();
        CutDescriptor cutDescriptor = testContext.getCutDescriptor();

        checkState(cutDescriptor != null,
                "Test class '%s' does not declare a @Cut class.",
                testClassName);

        checkState(testContext.getConstructorCount() == 1,
                "Class under test '%s' declared in test class '%s' has %s constructors. "
                + "Please insure that the class under test has one and only one constructor.",
                cutDescriptor.getTypeName(), testClassName, testContext.getConstructorCount());

        fieldDescriptors.parallelStream().forEach(p -> {

            Class<?> fieldType = p.getType();
            String fieldName = p.getName();
            String fieldTypeName = p.getTypeName();

            checkState(!isFinal(fieldType.getModifiers()),
                    "Field '%s' in test class '%s' can not be mocked because '%s'"
                    + " is a final class.",
                    fieldName, testClassName, fieldTypeName);

            checkState(!p.hasAnyAnnotation(Real.class, Inject.class),
                    "Field '%s' in test class '%s' is not annotated with @Fake. "
                    + "Note that @Real and @Inject annotations not supported for "
                    + "unit tests. Please use @Fake instead.",
                    fieldName, fieldTypeName
            );
        });
    }

    @Override
    public void wiring() {
        CutDescriptor cutDescriptor = testContext.getCutDescriptor();
        String testClassName = testContext.getTestClassName();
        String cutClassName = cutDescriptor.getTypeName();
        Collection<ParameterDescriptor> fieldDescriptors
                = testContext.getParamaterDescriptors().values();

        fieldDescriptors.parallelStream().forEach(p -> {
            Optional instance = p.getInstance();
            if (!instance.isPresent()) {
                String paramTypeName = p.getTypeName();
                logger.warn("Improper wiring detected. Class under test '{}' defined "
                        + "in '{}' declars constructor argument of type '{}' but '{}' "
                        + "does not define a field of type '{}' annotated with @Fake.",
                        cutClassName, testClassName, paramTypeName, testClassName, paramTypeName);
            }

        });
    }

}
