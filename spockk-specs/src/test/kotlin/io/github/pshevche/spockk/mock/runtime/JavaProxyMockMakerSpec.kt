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
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.mock.runtime.ByteBuddyMockMakerSpec.ByteBuddyTestClassLoader
import org.spockframework.mock.CannotCreateMockException
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.mock.MockMakers
import java.lang.reflect.Proxy

class JavaProxyMockMakerSpec : Specification() {

  @MigratedFrom("org.spockframework.mock.runtime.JavaProxyMockMakerSpec#Verify ID and IMockMakerSettings")
  fun `Verify ID and IMockMakerSettings`() {
    expect
    MockMakers.javaProxy.mockMakerId.toString() == "java-proxy"
    MockMakers.javaProxy.toString() == "java-proxy default mock maker settings"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.JavaProxyMockMakerSpec#Use specific MockMaker javaProxy")
  fun `Use specific MockMaker javaProxy`() {
    `when`
    val m: Runnable = Mock(mapOf("mockMaker" to MockMakers.javaProxy))
    val mockClass = m.javaClass

    then
    Proxy.isProxyClass(mockClass)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.JavaProxyMockMakerSpec#Use specific MockMaker javaProxy for class shall fail")
  fun `Use specific MockMaker javaProxy for class shall fail`() {
    `when`
    Mock(mapOf("mockMaker" to MockMakers.javaProxy), ArrayList::class.java)

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.message == "Cannot create mock for class java.util.ArrayList. java-proxy: Cannot mock classes."
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.JavaProxyMockMakerSpec#Constructor args are not supported")
  fun `Constructor args are not supported`() {
    `when`
    Spy(mapOf("mockMaker" to MockMakers.javaProxy, "constructorArgs" to listOf("A")), Runnable::class.java)

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.message ==
      "Cannot create mock for interface java.lang.Runnable. java-proxy: Explicit constructor arguments are not supported."
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom(
    "org.spockframework.mock.runtime.JavaProxyMockMakerSpec#Mocking interface from different classloader shall fail for javaProxy MockMaker"
  )
  fun `Mocking interface from different classloader shall fail for javaProxy MockMaker`() {
    given
    val tempClassLoader = ByteBuddyTestClassLoader()
    val interfaceClass = tempClassLoader.defineInterface("Interface")

    `when`
    Mock(mapOf("mockMaker" to MockMakers.javaProxy), interfaceClass)

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.message!!.startsWith(
      "Cannot create mock for interface Interface. java-proxy: The class Interface is not visible by the classloader"
    )
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom(
    "org.spockframework.mock.runtime.JavaProxyMockMakerSpec#Mocking with additional interface from different classloader shall fail for javaProxy MockMaker"
  )
  fun `Mocking with additional interface from different classloader shall fail for javaProxy MockMaker`() {
    given
    val tempClassLoader = ByteBuddyTestClassLoader()
    val additionalInterfaceClass = tempClassLoader.defineInterface("AdditionalInterface")

    `when`
    Mock(
      mapOf("mockMaker" to MockMakers.javaProxy, "additionalInterfaces" to listOf(additionalInterfaceClass)),
      Runnable::class.java
    )

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.message!!.startsWith(
      "Cannot create mock for interface java.lang.Runnable. java-proxy: " +
        "The class AdditionalInterface is not visible by the classloader"
    )
  }
}
