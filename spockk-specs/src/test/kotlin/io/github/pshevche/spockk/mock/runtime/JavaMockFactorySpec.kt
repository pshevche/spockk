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

package io.github.pshevche.spockk.mock.runtime

import groovy.util.Node
import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.mock.IMockMe
import io.github.pshevche.spockk.mock.MockMe
import org.spockframework.mock.MockImplementation
import org.spockframework.mock.MockNature
import org.spockframework.mock.runtime.JavaMockFactory
import org.spockframework.mock.runtime.MockConfiguration
import spock.lang.Specification

class JavaMockFactorySpec : Specification() {

  private val factory = JavaMockFactory()

  private fun createMock(type: Class<*>): Any? =
    factory.create(MockConfiguration("foo", type, MockNature.MOCK, MockImplementation.JAVA, emptyMap()), this)

  @MigratedFrom("org.spockframework.mock.runtime.JavaMockFactorySpec#can create mocks for interfaces")
  fun `can create mocks for interfaces`() {
    expect
    createMock(List::class.java) is List<*>
  }

  @MigratedFrom(
    "org.spockframework.mock.runtime.JavaMockFactorySpec#can create mocks for interfaces containing nested classes"
  )
  fun `can create mocks for interfaces containing nested classes`() {
    expect
    createMock(InterfaceWithNestedClass::class.java) is InterfaceWithNestedClass
  }

  @MigratedFrom("org.spockframework.mock.runtime.JavaMockFactorySpec#can create mocks for classes w/ parameterless constructor")
  fun `can create mocks for classes with parameterless constructor`() {
    expect
    createMock(ArrayList::class.java) is ArrayList<*>
  }

  @MigratedFrom("org.spockframework.mock.runtime.JavaMockFactorySpec#can create mocks for classes wo/ parameterless constructor")
  fun `can create mocks for classes without parameterless constructor`() {
    expect
    createMock(Node::class.java) is Node
  }

  @MigratedFrom("org.spockframework.mock.runtime.JavaMockFactorySpec#can create mocks for interfaces defined in Groovy")
  fun `can create mocks for interfaces defined in Kotlin`() {
    expect
    createMock(IMockMe::class.java) is IMockMe
  }

  @MigratedFrom("org.spockframework.mock.runtime.JavaMockFactorySpec#can create mocks for classes defined in Groovy")
  fun `can create mocks for classes defined in Kotlin`() {
    expect
    createMock(MockMe::class.java) is MockMe
  }
}

interface InterfaceWithNestedClass {
  class Service private constructor() {
    companion object {
      fun getInstance(): InterfaceWithNestedClass? = null
    }
  }
}
