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

import groovy.lang.Closure
import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.mock.CannotCreateMockException
import spock.lang.IgnoreIf
import spock.lang.PendingFeature
import spock.lang.Requires
import spock.lang.Specification
import spock.mock.DetachedMockFactory
import spock.mock.MockMakers
import java.lang.reflect.Proxy
import java.util.concurrent.Callable

class Java21Compatible(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: Any?): Boolean = Runtime.version().feature() >= 21
}

@IgnoreIf(value = Java21Compatible::class, reason = "Cglib doesn't support running on Java 21+")
class CglibMockMakerSpec : Specification() {

  @MigratedFrom("org.spockframework.mock.runtime.CglibMockMakerSpec#Verify ID and IMockMakerSettings")
  fun `Verify ID and IMockMakerSettings`() {
    expect
    MockMakers.cglib.mockMakerId.toString() == "cglib"
    MockMakers.cglib.toString() == "cglib default mock maker settings"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.CglibMockMakerSpec#Use specific MockMaker cglib")
  fun `Use specific MockMaker cglib`() {
    `when`
    val m: Runnable = Mock(mapOf("mockMaker" to MockMakers.cglib))
    val mockClass = m.javaClass

    then
    !Proxy.isProxyClass(mockClass)
    mockClass.name.contains("\$\$EnhancerByCGLIB\$\$")
  }

  @MigratedFrom("org.spockframework.mock.runtime.CglibMockMakerSpec#Use specific MockMaker cglib with DetachedMockFactory")
  fun `Use specific MockMaker cglib with DetachedMockFactory`() {
    given
    val factory = DetachedMockFactory()

    `when`
    val m = factory.Mock(mapOf("mockMaker" to MockMakers.cglib), Runnable::class.java)
    val mockClass = m.javaClass

    then
    !Proxy.isProxyClass(mockClass)
    mockClass.name.contains("\$\$EnhancerByCGLIB\$\$")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.CglibMockMakerSpec#Use specific MockMaker cglib intercept call for interface")
  fun `Use specific MockMaker cglib intercept call for interface`() {
    given
    val m: Callable<Int> = Mock(mapOf("mockMaker" to MockMakers.cglib))

    `when`
    val result = m.call()

    then
    1 * m.call() returns 1
    result == 1
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.CglibMockMakerSpec#Use specific MockMaker cglib intercept call for class")
  fun `Use specific MockMaker cglib intercept call for class`() {
    `when`
    val m: java.util.ArrayList<Int> = Mock(mapOf("mockMaker" to MockMakers.cglib))
    m.get(any()) returns 1

    then
    m.get(0) == 1
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.CglibMockMakerSpec#Final classes are not supported")
  fun `Final classes are not supported`() {
    `when`
    Mock(mapOf("mockMaker" to MockMakers.cglib), StringBuilder::class.java)

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.message == "Cannot create mock for class java.lang.StringBuilder. cglib: Cannot mock final classes."
  }
}

class Java21CompatibleRequired(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: Any?): Boolean = Runtime.version().feature() >= 21
}

@Requires(value = Java21CompatibleRequired::class, reason = "Cglib doesn't support running on Java 21+")
class CglibMockMakerUnsupportedSpec : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom(
    "org.spockframework.mock.runtime.CglibMockMakerSpec#Mocking with cglib on Java 21+ will be rejected with useful error message."
  )
  fun `Mocking with cglib on Java 21+ will be rejected with useful error message`() {
    `when`
    Mock(mapOf("mockMaker" to MockMakers.cglib), List::class.java)

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.message == "Cannot create mock for interface java.util.List. cglib: Mocking with cglib is not supported on Java 21 or newer."
  }
}
