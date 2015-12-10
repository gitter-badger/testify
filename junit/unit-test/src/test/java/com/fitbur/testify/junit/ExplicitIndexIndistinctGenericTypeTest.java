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
import com.fitbur.testify.junit.fixture.ExplicitIndexIndistinctGenericType;
import com.fitbur.testify.junit.fixture.collaborator.Hello;
import javax.inject.Provider;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import com.fitbur.testify.Fake;

/**
 *
 * @author saden
 */
@RunWith(UnitTestRunner.class)
public class ExplicitIndexIndistinctGenericTypeTest {

    @Cut
    ExplicitIndexIndistinctGenericType cut;

    @Fake(index = 0)
    Provider<Hello> english;

    @Fake
    Provider<Hello> spanish;

    @Before
    public void verifyInjections() {
        assertThat(cut).isNotNull();
        assertThat(english).isNotNull();
        assertThat(spanish).isNotNull();
        assertThat(cut.getHello1()).isSameAs(english);
        assertThat(cut.getHello2()).isSameAs(spanish);
    }

    @Test
    public void callToExecuteShouldReturnHelloHola() {
        String englishGreeting = "Hello";
        String spanishGreeting = "Hola";
        Hello englishInstance = mock(Hello.class);
        Hello spanishInstance = mock(Hello.class);

        given(english.get()).willReturn(englishInstance);
        given(spanish.get()).willReturn(spanishInstance);
        given(englishInstance.greet()).willReturn(englishGreeting);
        given(spanishInstance.greet()).willReturn(spanishGreeting);

        String result = cut.execute();

        assertThat(result).isEqualTo(englishGreeting + " " + spanishGreeting);
        verify(english).get();
        verify(spanish).get();
        verify(englishInstance).greet();
        verify(spanishInstance).greet();
        verifyNoMoreInteractions(english, spanish);
    }

    @Test
    public void callToExecuteShouldReturnHelloHola2() {
        String englishGreeting = "Hello";
        String spanishGreeting = "Hola";
        Hello englishInstance = mock(Hello.class);
        Hello spanishInstance = mock(Hello.class);

        given(english.get()).willReturn(englishInstance);
        given(spanish.get()).willReturn(spanishInstance);
        given(englishInstance.greet()).willReturn(englishGreeting);
        given(spanishInstance.greet()).willReturn(spanishGreeting);

        String result = cut.execute();

        assertThat(result).isEqualTo(englishGreeting + " " + spanishGreeting);
        verify(english).get();
        verify(spanish).get();
        verify(englishInstance).greet();
        verify(spanishInstance).greet();
        verifyNoMoreInteractions(english, spanish);
    }

}
