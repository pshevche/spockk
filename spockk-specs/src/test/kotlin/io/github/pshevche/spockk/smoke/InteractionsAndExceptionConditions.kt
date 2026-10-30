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

package io.github.pshevche.spockk.smoke

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.Mock
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import spock.lang.Specification

class InteractionsAndExceptionConditions : Specification() {

  @MigratedFrom("org.spockframework.smoke.InteractionsAndExceptionConditions#interaction followed by exception condition")
  fun `interaction followed by exception condition`() {
    val list: MutableList<Int> = Mock()

    `when`
    list.add(1)
    throw RuntimeException()

    then
    1 * list.add(1)
    thrown(RuntimeException::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.InteractionsAndExceptionConditions#exception condition followed by interaction")
  fun `exception condition followed by interaction`() {
    val list: MutableList<Int> = Mock()

    `when`
    list.add(1)
    throw RuntimeException()

    then
    thrown(RuntimeException::class.java)
    1 * list.add(1)
  }
}
