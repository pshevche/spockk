/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.pshevche.spockk.smoke.mock

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.Mock
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.spockframework.mock.TooFewInvocationsError
import spock.lang.FailsWith
import spock.lang.Specification

class MockingMethodsWithVarArgParameters : Specification() {

  @MigratedFrom(
    "org.spockframework.smoke.mock.MockingMethodsWithVarArgParameters#interactions of methods callable in vararg-style can be written in vararg-style"
  )
  fun `interactions of methods callable in vararg-style can be written in vararg-style`(clazz: Class<out KotlinVarArgParameter>) {
    val mock = Mock(clazz)

    `when`
    mock.foo(1, "one", "two", "three")

    then
    1 * mock.foo(1, "one", "two", "three")

    where
    variable(clazz).from(KotlinVarArgParameter::class.java, KotlinArrayParameter::class.java)
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.MockingMethodsWithVarArgParameters#interactions of methods callable in vararg-style can be written in array-style"
  )
  fun `interactions of methods callable in vararg-style can be written in array-style`(clazz: Class<out KotlinVarArgParameter>) {
    val mock = Mock(clazz)

    `when`
    mock.foo(1, "one", "two", "three")

    then
    1 * mock.foo(1, *arrayOf("one", "two", "three"))

    where
    variable(clazz).from(KotlinVarArgParameter::class.java, KotlinArrayParameter::class.java)
  }

  @FailsWith(TooFewInvocationsError::class)
  @MigratedFrom(
    "org.spockframework.smoke.mock.MockingMethodsWithVarArgParameters#one more argument than constraints - failing example"
  )
  fun `one more argument than constraints - failing example`() {
    val mock = Mock(KotlinVarArgParameter::class.java)

    `when`
    mock.foo(1, "one")

    then
    1 * mock.foo(1)
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.MockingMethodsWithVarArgParameters#vararg value that is omitted in invocation can be omitted in interaction"
  )
  fun `vararg value that is omitted in invocation can be omitted in interaction`() {
    val mock = Mock(KotlinVarArgParameter::class.java)

    `when`
    mock.foo(1)

    then
    1 * mock.foo(1)
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.MockingMethodsWithVarArgParameters#one more argument than constraints - passing example"
  )
  fun `one more argument than constraints - passing example`() {
    val mock = Mock(KotlinVarArgParameter::class.java)

    `when`
    mock.foo(1)

    then
    1 * mock.foo(1)
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.MockingMethodsWithVarArgParameters#equal number of arguments and constraints - passing example"
  )
  fun `equal number of arguments and constraints - passing example`() {
    val mock = Mock(KotlinVarArgParameter::class.java)

    `when`
    mock.foo(1, "one")

    then
    1 * mock.foo(1, "one")
  }

  @FailsWith(TooFewInvocationsError::class)
  @MigratedFrom(
    "org.spockframework.smoke.mock.MockingMethodsWithVarArgParameters#equal number of arguments and constraints - failing example"
  )
  fun `equal number of arguments and constraints - failing example`() {
    val mock = Mock(KotlinVarArgParameter::class.java)

    `when`
    mock.foo(1, "one", "two")

    then
    1 * mock.foo(1, "one")
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.MockingMethodsWithVarArgParameters#fewer arguments than constraints - passing example"
  )
  fun `fewer arguments than constraints - passing example`() {
    val mock = Mock(KotlinVarArgParameter::class.java)

    `when`
    mock.foo(1, "one", "two")

    then
    1 * mock.foo(1, "one", "two")
  }

  @FailsWith(TooFewInvocationsError::class)
  @MigratedFrom(
    "org.spockframework.smoke.mock.MockingMethodsWithVarArgParameters#fewer arguments than constraints - failing example"
  )
  fun `fewer arguments than constraints - failing example`() {
    val mock = Mock(KotlinVarArgParameter::class.java)

    `when`
    mock.foo(1, "one")

    then
    1 * mock.foo(1, "one", "two")
  }

  @FailsWith(TooFewInvocationsError::class)
  @MigratedFrom(
    "org.spockframework.smoke.mock.MockingMethodsWithVarArgParameters#vararg constraint satisfied but other constraint fails"
  )
  fun `vararg constraint satisfied but other constraint fails`() {
    val mock = Mock(KotlinVarArgParameter::class.java)

    `when`
    mock.foo(1, "one", "two")

    then
    1 * mock.foo(2, "one", "two")
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.MockingMethodsWithVarArgParameters#vararg value that is omitted in invocation can be specified as empty array in interaction"
  )
  fun `vararg value that is omitted in invocation can be specified as empty array in interaction`() {
    val mock = Mock(KotlinVarArgParameter::class.java)

    `when`
    mock.foo(1)

    then
    1 * mock.foo(1, *emptyArray<String>())
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.MockingMethodsWithVarArgParameters#vararg handling also works when element type is a primitive type"
  )
  fun `vararg handling also works when element type is a primitive type`(clazz: Class<out KotlinPrimitiveVarArgParameter>) {
    val mock = Mock(clazz)

    `when`
    mock.foo("one", 1, 2, 3)
    mock.foo("one", *intArrayOf(1, 2, 3))

    then
    2 * mock.foo("one", 1, 2, 3)

    `when`
    mock.foo("one", 1, 2, 3)
    mock.foo("one", *intArrayOf(1, 2, 3))

    then
    2 * mock.foo("one", *intArrayOf(1, 2, 3))

    where
    variable(clazz).from(KotlinPrimitiveVarArgParameter::class.java, KotlinPrimitiveArrayParameter::class.java)
  }
}

interface KotlinVarArgParameter {
  fun foo(i: Int, vararg strings: String)
}

interface KotlinArrayParameter : KotlinVarArgParameter {
  override fun foo(i: Int, strings: Array<out String>)
}

interface KotlinPrimitiveVarArgParameter {
  fun foo(str: String, vararg ints: Int)
}

interface KotlinPrimitiveArrayParameter : KotlinPrimitiveVarArgParameter {
  override fun foo(str: String, ints: IntArray)
}
