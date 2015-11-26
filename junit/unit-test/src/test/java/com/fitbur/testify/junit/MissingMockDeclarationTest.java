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
package com.fitbur.testify.junit;

import com.fitbur.testify.Cut;
import com.fitbur.testify.Mock;
import com.fitbur.testify.junit.fixture.ImplicitTypeDistinctTypes;
import com.fitbur.testify.junit.fixture.collaborator.Hello;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author saden
 */
@RunWith(UnitTestRunner.class)
public class MissingMockDeclarationTest {

    @Cut
    ImplicitTypeDistinctTypes cut;

    @Mock
    Hello hello;

    @Test(expected = NullPointerException.class)
    public void givenMissingMockShouldThrowException() {
        cut.execute();
    }

}
