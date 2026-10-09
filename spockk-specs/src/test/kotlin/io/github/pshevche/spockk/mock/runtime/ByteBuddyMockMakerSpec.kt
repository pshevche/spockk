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
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import net.bytebuddy.ByteBuddy
import net.bytebuddy.dynamic.loading.MultipleParentClassLoader
import org.spockframework.mock.CannotCreateMockException
import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.mock.DetachedMockFactory
import spock.mock.MockMakers
import java.lang.reflect.Proxy
import java.util.concurrent.Callable

class ByteBuddyMockMakerSpec : Specification() {

  @MigratedFrom("org.spockframework.mock.runtime.ByteBuddyMockMakerSpec#Verify ID and IMockMakerSettings")
  fun `Verify ID and IMockMakerSettings`() {
    expect
    MockMakers.byteBuddy.mockMakerId.toString() == "byte-buddy"
    MockMakers.byteBuddy.toString() == "byte-buddy default mock maker settings"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.ByteBuddyMockMakerSpec#Use specific MockMaker byteBuddy")
  fun `Use specific MockMaker byteBuddy`() {
    `when`
    val m: Runnable = Mock(mapOf("mockMaker" to MockMakers.byteBuddy))
    val mockClass = m.javaClass

    then
    !Proxy.isProxyClass(mockClass)
    mockClass.name.contains("\$SpockMock\$")
  }

  @MigratedFrom("org.spockframework.mock.runtime.ByteBuddyMockMakerSpec#Use specific MockMaker byteBuddy with DetachedMockFactory")
  fun `Use specific MockMaker byteBuddy with DetachedMockFactory`() {
    given
    val factory = DetachedMockFactory()

    `when`
    val m = factory.Mock(mapOf("mockMaker" to MockMakers.byteBuddy), Runnable::class.java)
    val mockClass = m.javaClass

    then
    !Proxy.isProxyClass(mockClass)
    mockClass.name.contains("\$SpockMock\$")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.ByteBuddyMockMakerSpec#Use specific MockMaker byteBuddy intercept call for interface")
  fun `Use specific MockMaker byteBuddy intercept call for interface`() {
    given
    val m: Callable<Int> = Mock(mapOf("mockMaker" to MockMakers.byteBuddy))

    `when`
    val result = m.call()

    then
    1 * m.call() returns 1
    result == 1
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.ByteBuddyMockMakerSpec#Use specific MockMaker byteBuddy intercept call for class")
  fun `Use specific MockMaker byteBuddy intercept call for class`() {
    given
    val m: java.util.ArrayList<Int> = Mock(mapOf("mockMaker" to MockMakers.byteBuddy))

    `when`
    val first = m.get(0)

    then
    m.get(any()) returns 1
    first == 1
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.ByteBuddyMockMakerSpec#Final classes are not supported")
  fun `Final classes are not supported`() {
    `when`
    Mock(mapOf("mockMaker" to MockMakers.byteBuddy), StringBuilder::class.java)

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.message == "Cannot create mock for class java.lang.StringBuilder. byte-buddy: Cannot mock final classes."
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.ByteBuddyMockMakerSpec#Mock class")
  fun `Mock class`() {
    `when`
    val s: DataClass = Mock(mapOf("mockMaker" to MockMakers.byteBuddy))

    then
    !s.boolField
    s.stringField == null
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.ByteBuddyMockMakerSpec#Stub class")
  fun `Stub class`() {
    `when`
    val s: DataClass = Stub(mapOf("mockMaker" to MockMakers.byteBuddy))

    then
    !s.boolField
    s.stringField == ""
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.ByteBuddyMockMakerSpec#Spy class")
  fun `Spy class`() {
    `when`
    val s: DataClass = Spy(mapOf("mockMaker" to MockMakers.byteBuddy))

    then
    s.boolField
    s.stringField == "data"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.ByteBuddyMockMakerSpec#Spy class with empty constructorArgs")
  fun `Spy class with empty constructorArgs`() {
    `when`
    val s: DataClass = Spy(mapOf("mockMaker" to MockMakers.byteBuddy, "constructorArgs" to emptyList<Any>()))

    then
    s.boolField
    s.stringField == "data"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.ByteBuddyMockMakerSpec#Spy class with constructorArgs")
  fun `Spy class with constructorArgs`() {
    `when`
    val s: DataClass = Spy(mapOf("mockMaker" to MockMakers.byteBuddy, "constructorArgs" to listOf(false, "data2")))

    then
    !s.boolField
    s.stringField == "data2"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.ByteBuddyMockMakerSpec#Spy class without default constructor does work")
  fun `Spy class without default constructor does work`() {
    `when`
    val s: ClassWithoutDefaultConstructor = Spy(mapOf("mockMaker" to MockMakers.byteBuddy))

    then
    s.stringField == "data"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom(
    "org.spockframework.mock.runtime.ByteBuddyMockMakerSpec#Mocking interface from different classloader shall fail for byteBuddy MockMaker"
  )
  fun `Mocking interface from different classloader shall fail for byteBuddy MockMaker`() {
    given
    val tempClassLoader = ByteBuddyTestClassLoader()
    val interfaceClass = tempClassLoader.defineInterface("Interface")

    `when`
    Mock(mapOf("mockMaker" to MockMakers.byteBuddy), interfaceClass)

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.message!!.startsWith(
      "Cannot create mock for interface Interface. byte-buddy: The class Interface is not visible by the classloader"
    )
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom(
    "org.spockframework.mock.runtime.ByteBuddyMockMakerSpec#Mocking with additional interface from different classloader shall fail for byteBuddy MockMaker"
  )
  fun `Mocking with additional interface from different classloader shall fail for byteBuddy MockMaker`() {
    given
    val tempClassLoader = ByteBuddyTestClassLoader()
    val additionalInterfaceClass = tempClassLoader.defineInterface("AdditionalInterface")

    `when`
    Mock(
      mapOf("mockMaker" to MockMakers.byteBuddy, "additionalInterfaces" to listOf(additionalInterfaceClass)),
      Runnable::class.java
    )

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.message!!.startsWith(
      "Cannot create mock for interface java.lang.Runnable. byte-buddy: " +
        "The class AdditionalInterface is not visible by the classloader"
    )
  }

  @Issue("https://github.com/spockframework/spock/issues/2017")
  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom(
    "org.spockframework.mock.runtime.ByteBuddyMockMakerSpec#ByteBuddy Mocks with interfaces that are not visible to the mocked type's classloader"
  )
  fun `ByteBuddy Mocks with interfaces that are not visible to the mocked type's classloader`() {
    given
    val testCl1 = ByteBuddyTestClassLoader()
    val testCl2 = ByteBuddyTestClassLoader()
    val fooBar = testCl1.defineInterface("foo.Bar")
    val mocked = testCl2.defineInterface("iam.Mocked")
    // Only validates that the combined loader is set up; Spock resolves the mocked type's loader itself.
    MultipleParentClassLoader(listOf(javaClass.classLoader, testCl1, testCl2))

    `when`
    Mock(mapOf("mockMaker" to MockMakers.byteBuddy, "additionalInterfaces" to listOf(fooBar)), mocked)

    then
    noExceptionThrown()
  }

  @MigratedFrom("org.spockframework.mock.runtime.ByteBuddyMockMakerSpec#Local mocks are detected correctly")
  fun `Local mocks are detected correctly`(
    targetClass: Class<*>,
    additionalInterfaces: Collection<Class<*>>,
    expected: Boolean
  ) {
    expect
    isLocalMock(targetClass, additionalInterfaces) == expected

    where
    targetClass          ; additionalInterfaces                                       ; expected
    DataClass::class.java ; emptyList<Class<*>>()                                      ; true
    DataClass::class.java ; listOf(java.io.Serializable::class.java)                   ; true
    DataClass::class.java ; listOf(ByteBuddyTestClassLoader().defineInterface("foo")) ; false
  }

  // ByteBuddyMockFactory is package-private in Spock, so it is reached by reflection.
  private fun isLocalMock(targetClass: Class<*>, additionalInterfaces: Collection<Class<*>>): Boolean {
    val method = Class.forName("org.spockframework.mock.runtime.ByteBuddyMockFactory")
      .getDeclaredMethod("isLocalMock", Class::class.java, Collection::class.java)
      .apply { isAccessible = true }
    return method.invoke(null, targetClass, additionalInterfaces) as Boolean
  }

  class ByteBuddyTestClassLoader : ClassLoader() {
    private val cache = HashMap<String, Class<*>>()

    @Synchronized
    fun defineInterface(name: String): Class<*> = cache.getOrPut(name) {
      val bytes = ByteBuddy().makeInterface().name(name).make().bytes
      defineClass(name, bytes, 0, bytes.size)
    }
  }
}

open class DataClass(open val boolField: Boolean, open val stringField: String?) {
  constructor() : this(true, "data")
}

open class ClassWithoutDefaultConstructor(@Suppress("UNUSED_PARAMETER") arg: String) {
  open val stringField: String? = "data"
}
