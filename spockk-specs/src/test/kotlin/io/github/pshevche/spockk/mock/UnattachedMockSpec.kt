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

package io.github.pshevche.spockk.mock

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import spock.lang.Specification
import spock.mock.DetachedMockFactory

class UnattachedMockSpec : Specification() {

  private val factory = DetachedMockFactory()

  @MigratedFrom("org.spockframework.mock.UnattachedMockSpec#Mock(class)")
  fun `Mock(class)`() {
    given
    val mock = factory.Mock(SomeInterface::class.java)

    `when`
    val result = mock.hello()

    then
    result == null
  }

  @MigratedFrom("org.spockframework.mock.UnattachedMockSpec#Stub(class)")
  fun `Stub(class)`() {
    given
    val stub = factory.Stub(SomeInterface::class.java)

    `when`
    val result = stub.hello()
    val result2 = stub.hello()

    then
    result == ""
    result2 == ""
  }

  @MigratedFrom("org.spockframework.mock.UnattachedMockSpec#Spy(class)")
  fun `Spy(class)`() {
    given
    val spy: SomeInterface = factory.Spy(SomeClass::class.java)

    `when`
    val result = spy.hello()

    then
    result == "world"
  }

  @MigratedFrom("org.spockframework.mock.UnattachedMockSpec#Spy(obj)")
  fun `Spy(obj)`() {
    given
    val spy: SomeInterface = factory.Spy(SomeClass("bar"))

    `when`
    val result = spy.hello()

    then
    result == "bar"
  }
}

interface SomeInterface {
  fun hello(): String?
}

class SomeClass(private val answer: String = "world") : SomeInterface {
  override fun hello(): String? = answer
}
