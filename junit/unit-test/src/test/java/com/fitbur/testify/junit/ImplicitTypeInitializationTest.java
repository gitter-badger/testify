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
import com.fitbur.testify.junit.fixture.ImplicitType;
import com.fitbur.testify.junit.fixture.collaborator.Hello;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import org.mockito.internal.util.MockUtil;
import com.fitbur.testify.Fake;

/**
 *
 * @author saden
 */
@RunWith(UnitTestRunner.class)
public class ImplicitTypeInitializationTest {

    @Cut
    ImplicitType cut;

    @Fake
    Hello hello = new Hello();

    @Before
    public void verifyInjections() {
        assertThat(cut).isNotNull();
        assertThat(hello).isNotNull();
        assertThat(cut.getHello()).isSameAs(hello);
        assertThat(new MockUtil().isMock(hello)).isTrue();
    }

    @Test
    public void givenNothingClassToExecuteShouldReturnHello() {
        String helloGreeting = "Hello";
        given(hello.greet()).willReturn(helloGreeting);

        String result = cut.execute();

        assertThat(result).isEqualTo(helloGreeting);
        verify(hello).greet();
        verifyNoMoreInteractions(hello);
    }

}
