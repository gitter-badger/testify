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
package com.fitbur.testify;

import com.fitbur.testify.descriptor.CutDescriptor;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.descriptor.ParameterDescriptor;
import java.util.Set;

/**
 * An interfaces that defines methods for creating concrete objects to be
 * injected into a test class.
 *
 * @author saden
 */
public interface TestReifier {

    Object reifyField(FieldDescriptor fieldDescriptor, ParameterDescriptor parameterDescriptor);

    Object reifyCut(CutDescriptor cutDescriptor, Object[] arguments);

    default void reifyTest(Set< FieldDescriptor> fieldDescriptors) {
    }

}
