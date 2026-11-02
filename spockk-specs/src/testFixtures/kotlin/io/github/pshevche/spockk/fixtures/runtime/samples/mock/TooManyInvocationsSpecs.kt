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

package io.github.pshevche.spockk.fixtures.runtime.samples.mock

import io.github.pshevche.spockk.lang.Mock
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import spock.lang.Specification

@Suppress("FunctionNaming")
class TooManyInvocationsSpecs : Specification() {
  fun `shows matched invocations, ordered by last occurrence`() {
    val list = Mock(MutableList::class.java) as MutableList<Int>

    `when`
    list.add(2)
    list.add(1)
    list.add(4)
    list.add(2)

    then
    3 * list.add(any())
  }

  fun `scope-level unmatched invocations are not shown without unsatisfied interactions`() {
    val list = Mock(MutableList::class.java) as MutableList<Int>

    `when`
    list.add(2)
    list.add(1)

    then
    0 * list.add(1)
  }

  fun `unmatched invocations on different methods are excluded`() {
    val list = Mock(MutableList::class.java) as MutableList<Int>

    `when`
    list.get(0)
    list.size
    list.add(1)

    then
    0 * list.add(1)
  }

  fun `both unmatched invocations and unsatisfied interactions are shown together`() {
    val list = Mock(MutableList::class.java) as MutableList<Int>

    `when`
    list.add(2)
    list.add(1)

    then
    1 * list.add(42)
    0 * list.add(1)
  }

  fun `no unmatched section when no relevant context exists`() {
    val list = Mock(MutableList::class.java) as MutableList<Int>

    `when`
    list.add(1)

    then
    0 * list.add(1)
  }
}
