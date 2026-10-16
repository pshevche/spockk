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

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import org.spockframework.mock.CallRealMethodResponse
import org.spockframework.mock.EmptyOrDummyResponse
import org.spockframework.mock.IDefaultResponse
import org.spockframework.mock.IMockInvocation
import org.spockframework.mock.MockImplementation
import org.spockframework.mock.MockNature
import org.spockframework.mock.ZeroOrNullResponse
import org.spockframework.mock.runtime.MockConfiguration
import spock.lang.Specification
import spock.mock.MockMakers

class MockConfigurationSpec : Specification() {

  @MigratedFrom("org.spockframework.mock.runtime.MockConfigurationSpec#defaults for Mock nature")
  fun `defaults for Mock nature`() {
    given
    val options = MockConfiguration("foo", List::class.java, MockNature.MOCK, MockImplementation.JAVA, emptyMap())

    expect
    options.nature == MockNature.MOCK
    options.defaultResponse is ZeroOrNullResponse
    options.isGlobal == false
    options.isVerified == true
    options.isUseObjenesis == true
  }

  @MigratedFrom("org.spockframework.mock.runtime.MockConfigurationSpec#defaults for Stub nature")
  fun `defaults for Stub nature`() {
    given
    val options = MockConfiguration("foo", List::class.java, MockNature.STUB, MockImplementation.JAVA, emptyMap())

    expect
    options.nature == MockNature.STUB
    options.defaultResponse is EmptyOrDummyResponse
    options.isGlobal == false
    options.isVerified == false
    options.isUseObjenesis == true
  }

  @MigratedFrom("org.spockframework.mock.runtime.MockConfigurationSpec#defaults for Spy nature")
  fun `defaults for Spy nature`() {
    given
    val options = MockConfiguration("foo", List::class.java, MockNature.SPY, MockImplementation.JAVA, emptyMap())

    expect
    options.nature == MockNature.SPY
    options.defaultResponse is CallRealMethodResponse
    options.isGlobal == false
    options.isVerified == true
    options.isUseObjenesis == false
  }

  @MigratedFrom("org.spockframework.mock.runtime.MockConfigurationSpec#set options via map")
  fun `set options via map`() {
    given
    val defaultResponse = object : IDefaultResponse {
      override fun respond(invocation: IMockInvocation): Any? = null
    }
    val map = mapOf<String, Any?>(
      "name" to "foo",
      "type" to List::class.java,
      "nature" to MockNature.SPY,
      "implementation" to MockImplementation.GROOVY,
      "constructorArgs" to listOf("foo", "bar"),
      "defaultResponse" to defaultResponse,
      "global" to true,
      "verified" to false,
      "useObjenesis" to true,
      "mockMaker" to MockMakers.javaProxy
    )
    val options = MockConfiguration("bar", Map::class.java, MockNature.MOCK, MockImplementation.JAVA, map)

    expect
    options.name == "foo"
    options.type == List::class.java
    options.nature == MockNature.SPY
    options.implementation == MockImplementation.GROOVY
    options.constructorArgs == listOf("foo", "bar")
    options.defaultResponse === defaultResponse
    options.isGlobal == true
    options.isVerified == false
    options.isUseObjenesis == true
    options.mockMaker == MockMakers.javaProxy
  }
}
