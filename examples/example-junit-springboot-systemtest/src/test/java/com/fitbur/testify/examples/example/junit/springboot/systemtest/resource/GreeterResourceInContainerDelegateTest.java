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
package com.fitbur.testify.examples.example.junit.springboot.systemtest.resource;

import com.fitbur.testify.App;
import com.fitbur.testify.Cut;
import com.fitbur.testify.Real;
import com.fitbur.testify.examples.example.junit.springboot.systemtest.GreeterApplication;
import com.fitbur.testify.examples.example.junit.springboot.systemtest.service.GreetingService;
import com.fitbur.testify.junit.system.SpringBootSystemTest;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 *
 * @author saden
 */
@App(GreeterApplication.class)
@RunWith(SpringBootSystemTest.class)
public class GreeterResourceInContainerDelegateTest {

    @Cut
    GreeterResource cut;

    @Real(true)
    GreetingService greetingService;

    @Test
    public void callToGreetShouldReturnHello() {
        //Arrange
        String phrase = "Hello";

        //Act
        String result = cut.greet();

        //Assert
        assertThat(result).isEqualTo(phrase);
    }

    @Test
    public void callToGreetShouldReturnCaio() {
        //Arrange
        String phrase = "Ciao";
        given(greetingService.phrase()).willReturn(phrase);

        //Act
        String result = cut.greet();

        //Assert
        assertThat(result).isEqualTo(phrase);
        verify(greetingService).phrase();
    }

}
