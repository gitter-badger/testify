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

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * An annotation used on single fields of a test class to denote the field as
 * the class under test and to inject mock instances of its collaborators. Note
 * that you can optionally enable spying on the class under test instance by
 * setting {@link  #value()} to true.
 *
 * <p>
 * Example:
 *
 * <pre>
 *<code>
 *public class GreetingService {
 *
 * public String greet() {
 *   return sayHello();
 * }
 *
 * public String sayHello() {
 *   return "Hello!";
 * }
 *}
 *
 *public class GreetingServiceTest {
 *
 *{@code @Cut}
 * GreetingService cut;
 *
 *
 *{@code @Test}
 * public void verifyInjection() {
 *   assertThat(cut)
 *     .isNotNull()
 *     .isInstanceOf(MockitoSpy.class);
 * }
 *
 *{@code @Test}
 * public void callToGreetShouldReturnHello() {
 *   String greeting = "Hello!";
 *
 *   String result = cut.greet();
 *
 *   assertThat(result).isEqualTo(greeting);
 *   verify(cut).greet();
 *   verify(cut).sayHello();
 * }
 *
 *{@code @Test}
 * public void callToGreetShouldReturnHola() {
 *   String greeting = "Hola!";
 *   when(cut.sayHello()).thenReturn(greeting);
 *
 *   String result = cut.greet();
 *
 *   assertThat(result).isEqualTo(greeting);
 *   verify(cut).greet();
 *   verify(cut).sayHello();
 * }
 *}
 * </code>
 * </pre>
 *
 * @author saden
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD, FIELD, PARAMETER})
public @interface Cut {

    /**
     * Indicates whether the class under test should be a spied upon. This is
     * usefully if you wish to stub or verify calls to protected/private methods
     * of the the class under test. Be very careful how you use this since
     * spying on the class under test instance can lead to calls to the real
     * object which often is not desirable.
     *
     * <p>
     * By default spying on a class under test instance is set to false.
     *
     * <p>
     * @return true if class under test should be spied on.
     */
    boolean value() default false;
}
