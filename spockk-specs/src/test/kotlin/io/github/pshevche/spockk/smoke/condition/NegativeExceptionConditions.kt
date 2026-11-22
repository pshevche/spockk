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

package io.github.pshevche.spockk.smoke.condition

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.runtime.UnallowedExceptionThrownError
import spock.lang.FailsWith
import spock.lang.Specification
import java.io.IOException

class NegativeExceptionConditions : Specification() {

  @MigratedFrom("org.spockframework.smoke.condition.NegativeExceptionConditions#no exception allowed, none thrown")
  fun `no exception allowed, none thrown`() {
    `when`
    val x = 1

    then
    noExceptionThrown()
  }

  @FailsWith(UnallowedExceptionThrownError::class)
  @MigratedFrom("org.spockframework.smoke.condition.NegativeExceptionConditions#no exception allowed, IOException thrown")
  fun `no exception allowed, IOException thrown`() {
    `when`
    throw IOException()

    then
    noExceptionThrown()
  }

  @MigratedFrom("org.spockframework.smoke.condition.NegativeExceptionConditions#no IOException allowed, none thrown")
  fun `no IOException allowed, none thrown`() {
    `when`
    val x = 1

    then
    notThrown(IOException::class.java)
  }

  @FailsWith(UnallowedExceptionThrownError::class)
  @MigratedFrom("org.spockframework.smoke.condition.NegativeExceptionConditions#no IOException allowed, IOException thrown")
  fun `no IOException allowed, IOException thrown`() {
    `when`
    throw IOException()

    then
    notThrown(IOException::class.java)
  }

  @FailsWith(RuntimeException::class)
  @MigratedFrom("org.spockframework.smoke.condition.NegativeExceptionConditions#no IOException allowed, other exception thrown")
  fun `no IOException allowed, other exception thrown`() {
    `when`
    throw RuntimeException()

    then
    notThrown(IOException::class.java)
  }
}
