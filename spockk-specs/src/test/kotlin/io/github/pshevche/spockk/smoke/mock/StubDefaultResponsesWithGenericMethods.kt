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
import io.github.pshevche.spockk.lang.Stub
import io.github.pshevche.spockk.lang.expect
import spock.lang.Specification

class StubDefaultResponsesWithGenericMethods : Specification() {

  interface Person<T> {
    val pet: T
  }

  open class Cat

  open class CatPerson<T> : Person<Cat> {
    override val pet: Cat
      get() = Cat()

    open val gadget: T?
      get() = null
  }

  open class Phone

  open class PhonePerson : CatPerson<Phone>()

  interface PersonHolder {
    val person: Person<Cat>
  }

  @MigratedFrom("org.spockframework.smoke.mock.StubDefaultResponsesWithGenericMethods#class that implements parameterized interface")
  fun `class that implements parameterized interface`() {
    val person = Stub(CatPerson::class.java)

    expect
    person.pet is Cat
  }

  @MigratedFrom("org.spockframework.smoke.mock.StubDefaultResponsesWithGenericMethods#class that extends parameterized class")
  fun `class that extends parameterized class`() {
    val person = Stub(PhonePerson::class.java)

    expect
    person.gadget is Phone
  }

  @MigratedFrom("org.spockframework.smoke.mock.StubDefaultResponsesWithGenericMethods#interface with parameterized return type")
  fun `interface with parameterized return type`() {
    val holder = Stub(PersonHolder::class.java)

    expect
    holder.person.pet is Cat
  }
}
