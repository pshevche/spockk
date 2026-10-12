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
import org.spockframework.mock.MockImplementation
import org.spockframework.mock.MockNature
import org.spockframework.mock.runtime.JavaMockFactory
import org.spockframework.mock.runtime.MockConfiguration
import spock.lang.Issue
import spock.lang.Specification
import java.lang.reflect.Type

class JavaMockFactorySpec : Specification() {

  private val factory = JavaMockFactory()

  private fun create(type: Type): Any? = factory.create(
    MockConfiguration("foo", type, MockNature.MOCK, MockImplementation.JAVA, emptyMap<String, Any>()),
    this,
  )

  @MigratedFrom("org.spockframework.mock.runtime.JavaMockFactorySpec#can create mocks for interfaces")
  fun `can create mocks for interfaces`() {
    expect
    create(List::class.java) is List<*>
  }

  @Issue("https://github.com/spockframework/spock/issues/349")
  @MigratedFrom("org.spockframework.mock.runtime.JavaMockFactorySpec#can create mocks for interfaces containing nested classes")
  fun `can create mocks for interfaces containing nested classes`() {
    expect
    create(InterfaceWithNestedClass::class.java) is InterfaceWithNestedClass
  }

  @MigratedFrom("org.spockframework.mock.runtime.JavaMockFactorySpec#can create mocks for classes w/ parameterless constructor")
  fun `can create mocks for classes w∕ parameterless constructor`() {
    expect
    create(ArrayList::class.java) is ArrayList<*>
  }

  @MigratedFrom("org.spockframework.mock.runtime.JavaMockFactorySpec#can create mocks for classes wo/ parameterless constructor")
  fun `can create mocks for classes wo∕ parameterless constructor`() {
    expect
    create(Node::class.java) is Node
  }

  @MigratedFrom("org.spockframework.mock.runtime.JavaMockFactorySpec#can create mocks for interfaces defined in Groovy")
  fun `can create mocks for interfaces defined in Groovy`() {
    expect
    create(IMockMe::class.java) is IMockMe
  }

  @MigratedFrom("org.spockframework.mock.runtime.JavaMockFactorySpec#can create mocks for classes defined in Groovy")
  fun `can create mocks for classes defined in Groovy`() {
    expect
    create(MockMe::class.java) is MockMe
  }
}

interface InterfaceWithNestedClass {
  class Service private constructor() {
    companion object {
      fun getInstance(): InterfaceWithNestedClass? = null
    }
  }
}

interface IMockMe {
  fun foo(i: Int): Any?
}

open class MockMe : IMockMe {
  override fun foo(i: Int): Any? = null
}
