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
package com.fitbur.testify.junit;

import com.fitbur.testify.junit.fixture.ImplicitGenericType;
import com.fitbur.testify.junit.fixture.collaborator.Hello;
import com.fitbur.testify.Cut;
import com.fitbur.testify.Mock;
import javax.inject.Provider;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 *
 * @author saden
 */
@RunWith(UnitTestRunner.class)
public class ImplicitGenericTypeTest {

    @Cut
    ImplicitGenericType cut;

    @Mock
    Provider<Hello> collaborator;

    @Before
    public void verifyInjections() {
        assertThat(cut).isNotNull();
        assertThat(collaborator).isNotNull();
        assertThat(cut.getHello()).isSameAs(collaborator);
    }

    @Test
    public void givenNothingClassToExecuteShouldReturnHello() {
        String greeting = "Hello!";
        Hello hello = mock(Hello.class);
        given(collaborator.get()).willReturn(hello);
        given(hello.greet()).willReturn(greeting);

        String result = cut.execute();

        assertThat(result).isEqualTo(greeting);
        verify(collaborator).get();
        verify(hello).greet();
        verifyNoMoreInteractions(collaborator);
    }

}
