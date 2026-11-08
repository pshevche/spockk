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
import io.github.pshevche.spockk.lang.Spy
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.mock.TooFewInvocationsError
import spock.lang.FailsWith
import spock.lang.Specification

class PartialMocking : Specification() {

  @MigratedFrom("org.spockframework.smoke.mock.PartialMocking#can mock method on same object")
  fun `can mock method on same object`() {
    val persister = Spy(MessagePersister::class.java)

    `when`
    persister.receive("msg")

    then
    1 * persister.store("msg")
  }

  @MigratedFrom("org.spockframework.smoke.mock.PartialMocking#can stub method on same object")
  fun `can stub method on same object`() {
    val persister = Spy(MessagePersister()) {
      isStorable(any()) returns false
    }

    `when`
    persister.receive("msg")

    then
    0 * persister.store("msg")
  }

  @FailsWith(TooFewInvocationsError::class)
  @MigratedFrom("org.spockframework.smoke.mock.PartialMocking#fails if interaction not satisfied")
  fun `fails if interaction not satisfied`() {
    val persister = Spy(MessagePersister::class.java)

    `when`
    persister.receive("msg")

    then
    2 * persister.store("msg")
  }

  open class MessagePersister {
    open fun receive(msg: String) {
      if (isStorable(msg)) store(msg)
    }

    open fun isStorable(msg: Any?): Boolean = true

    open fun store(msg: String) {}
  }
}
