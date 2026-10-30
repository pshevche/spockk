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

package io.github.pshevche.spockk.mock.runtime.mockito

import groovy.lang.Closure
import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.anyMethod
import io.github.pshevche.spockk.lang.does
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import net.bytebuddy.ByteBuddy
import org.mockito.MockSettings
import org.mockito.Mockito
import org.mockito.exceptions.base.MockitoException
import org.spockframework.mock.CannotCreateMockException
import org.spockframework.mock.MockUtil
import org.spockframework.mock.runtime.mockito.MockitoMockMaker
import org.spockframework.mock.runtime.mockito.MockitoMockMakerSettings
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.mock.DetachedMockFactory
import spock.mock.MockMakers.mockito
import spock.util.environment.Jvm
import java.io.FileInputStream
import java.io.Serializable
import java.lang.reflect.Proxy
import java.util.concurrent.Callable

private const val ID = "mockito"

class MockitoMockMakerSpec : Specification() {

  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Verify simple ID and IMockMakerSettings")
  fun `Verify simple ID and IMockMakerSettings`() {
    expect
    mockito.mockMakerId.toString() == ID
    mockito.toString() == "$ID default mock maker settings"
  }

  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#MockitoMockMaker.asMockOrNull() on null returns null")
  fun `MockitoMockMaker asMockOrNull() on null returns null`() {
    given
    val maker = MockitoMockMaker()

    expect
    maker.asMockOrNull(null) == null
  }

  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Verify ID and IMockMakerSettings with Mockito settings")
  fun `Verify ID and IMockMakerSettings with Mockito settings`() {
    `when`
    val set = mockito(settings { })

    then
    set.mockMakerId.toString() == ID
    set.toString() == "$ID mock maker settings"
    set is MockitoMockMakerSettings
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Use specific MockMaker mockito")
  fun `Use specific MockMaker mockito`() {
    `when`
    val m: Runnable = Mock(mapOf("mockMaker" to mockito))
    val mockClass = m.javaClass

    then
    !Proxy.isProxyClass(mockClass)
    mockClass.name.contains("\$MockitoMock\$")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Use specific MockMaker mockito check is mock")
  fun `Use specific MockMaker mockito check is mock`() {
    given
    val mockUtil = MockUtil()

    `when`
    val m: Runnable = Mock(mapOf("mockMaker" to mockito))

    then
    mockUtil.isMock(m)
    val mockObj = mockUtil.asMock(m)
    mockObj != null
    mockObj.name == "m"
    mockObj.instance == m
    mockObj.type == Runnable::class.java
    mockObj.specification == this
  }

  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Use specific MockMaker mockito with DetachedMockFactory")
  fun `Use specific MockMaker mockito with DetachedMockFactory`() {
    given
    val factory = DetachedMockFactory()

    `when`
    val m = factory.Mock(mapOf("mockMaker" to mockito), Runnable::class.java)
    val mockClass = m.javaClass

    then
    !Proxy.isProxyClass(mockClass)
    mockClass.name.contains("\$MockitoMock\$")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Use specific MockMaker mockito intercept call for interface")
  fun `Use specific MockMaker mockito intercept call for interface`() {
    given
    val m: Callable<Int> = Mock(mapOf("mockMaker" to mockito))

    `when`
    val result = m.call()

    then
    1 * m.call() returns 1
    result == 1
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Use specific MockMaker mockito intercept call for class")
  fun `Use specific MockMaker mockito intercept call for class`() {
    `when`
    val m: java.util.ArrayList<Int> = Mock(mapOf("mockMaker" to mockito))
    m.get(any()) returns 1

    then
    m.get(0) == 1
    m.javaClass.name == "java.util.ArrayList"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Final classes are supported")
  fun `Final classes are supported`() {
    `when`
    val sb: StringBuilder = Mock(mapOf("mockMaker" to mockito))

    then
    sb.javaClass.name == "java.lang.StringBuilder"

    `when`
    val res = sb.append(true)

    then
    1 * sb.append(true) returns sb
    0 * sb.anyMethod()
    res == sb
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Final method is supported")
  fun `Final method is supported`() {
    given
    val mock: ClassWithFinalMethod = Mock(mapOf("mockMaker" to mockito))

    `when`
    val res = mock.finalMethod()

    then
    1 * mock.finalMethod() returns "MockValue"
    0 * mock.anyMethod()
    res == "MockValue"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Use constructor args")
  fun `Use constructor args`() {
    given
    val startList = listOf(1, 2, 3)

    `when`
    val spy: java.util.ArrayList<Int> = Spy(mapOf("mockMaker" to mockito, "constructorArgs" to listOf(startList)))

    then
    spy[0] == 1
    spy[1] == 2
    spy[2] == 3
    spy == startList

    `when`
    spy.clear()

    then
    1 * spy.clear() does { }
    // The clear should have been intercepted
    spy == startList

    `when`
    spy.clear()

    then
    spy != startList
    spy.size == 0
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Fails to construct object without default constructor")
  fun `Fails to construct object without default constructor`() {
    `when`
    Spy(mapOf("mockMaker" to mockito, "useObjenesis" to false), FileInputStream::class.java)

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.message == "Cannot create mock for class java.io.FileInputStream with mockito: " +
      "Unable to create instance of 'FileInputStream'.\nPlease ensure that the target class has a 0-arg constructor."
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Additional interfaces")
  fun `Additional interfaces`() {
    `when`
    val mock: java.util.ArrayList<Int> =
      Mock(mapOf("mockMaker" to mockito, "additionalInterfaces" to listOf(Runnable::class.java)))

    then
    mock is java.util.ArrayList<*>
    mock is Runnable

    `when`
    (mock as Runnable).run()

    then
    1 * (mock as Runnable).run()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Additional interfaces with final class shall fail")
  fun `Additional interfaces with final class shall fail`() {
    `when`
    Mock(
      mapOf("mockMaker" to mockito, "additionalInterfaces" to listOf(Runnable::class.java)),
      StringBuilder::class.java
    )

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.message == "Cannot create mock for class java.lang.StringBuilder. " +
      "mockito: Cannot mock final classes with additional interfaces."
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Additional mockito settings serializable")
  fun `Additional mockito settings serializable`() {
    `when`
    val mock: Runnable = Mock(mapOf("mockMaker" to mockito(settings { serializable() })))

    then
    mock is Serializable
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Additional settings invalid shall catch MockitoException")
  fun `Additional settings invalid shall catch MockitoException`() {
    `when`
    Mock(mapOf("mockMaker" to mockito(settings { serializable() })), StringBuilder::class.java)

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.message!!.replace("\r\n", "\n").startsWith(
      "Cannot create mock for class java.lang.StringBuilder with mockito: " +
        "Mockito cannot mock this class: class java.lang.StringBuilder.\n" +
        "Can not mock final classes with the following settings :\n" +
        " - explicit serialization (e.g. withSettings().serializable())\n" +
        " - extra interfaces (e.g. withSettings().extraInterfaces(...))"
    )
    ex.cause!!.javaClass.name.startsWith("org.mockito.exceptions.")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Spy method throws exception")
  fun `Spy method throws exception`() {
    given
    val mock: ClassThrowingException = Spy(mapOf("mockMaker" to mockito))

    `when`
    mock.throwsException()

    then
    val ex = thrown(UnsupportedOperationException::class.java)
    ex.message == "TestEx"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Mock enum")
  fun `Mock enum`() {
    given
    val e: TestEnum = Mock(mapOf("mockMaker" to mockito))

    `when`
    val res = e.method()

    then
    1 * e.method() returns "C"
    res == "C"
    e.name == null
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Access protected Groovy const should be accessible in Groovy 3&4 with mockito")
  fun `Access protected Groovy const should be accessible in Groovy 3&4 with mockito`() {
    `when`
    val mySpy: AccessProtectedSubClass = Spy(mapOf("mockMaker" to mockito))

    then
    mySpy.accessStaticFlag()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Access protected Groovy field should be accessible in Groovy 3&4 with mockito")
  fun `Access protected Groovy field should be accessible in Groovy 3&4 with mockito`() {
    `when`
    val mySpy: AccessProtectedSubClass = Spy(mapOf("mockMaker" to mockito))

    then
    mySpy.accessNonStaticFlag()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Access protected Groovy fields should be accessible in Groovy 3&4 although @Internal annotation is not copied due to inline mock")
  fun `Access protected Groovy fields should be accessible in Groovy 3&4 although @Internal annotation is not copied due to inline mock`() {
    `when`
    val mySpy: AccessProtectedSubClass = Spy(mapOf("mockMaker" to mockito(settings { withoutAnnotations() })))

    then
    mySpy.accessNonStaticFlag()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Mock class")
  fun `Mock class`() {
    `when`
    val s: DataClass = Mock(mapOf("mockMaker" to mockito))

    then
    !s.bool
    s.string == null
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Stub class")
  fun `Stub class`() {
    `when`
    val s: DataClass = Stub(mapOf("mockMaker" to mockito))

    then
    !s.bool
    s.string == ""
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Spy class")
  fun `Spy class`() {
    `when`
    val s: DataClass = Spy(mapOf("mockMaker" to mockito))

    then
    s.bool
    s.string == "data"
  }

  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Spy class with Mockito API")
  fun `Spy class with Mockito API`() {
    `when`
    val s: DataClass = Mockito.spy(DataClass::class.java)

    then
    s.bool
    s.string == "data"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Spy class with empty constructorArgs")
  fun `Spy class with empty constructorArgs`() {
    `when`
    val s: DataClass = Spy(mapOf("mockMaker" to mockito, "constructorArgs" to emptyList<Any>()))

    then
    s.bool
    s.string == "data"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Spy class with constructorArgs")
  fun `Spy class with constructorArgs`() {
    `when`
    val s: DataClass = Spy(mapOf("mockMaker" to mockito, "constructorArgs" to listOf(false, "data2")))

    then
    !s.bool
    s.string == "data2"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Spy class without default constructor does not work")
  fun `Spy class without default constructor does not work`() {
    `when`
    Spy(mapOf("mockMaker" to mockito), ClassWithoutDefaultConstructor::class.java)

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.cause!!.cause!!.message!!.contains("Please ensure that the target class has a 0-arg constructor.")
  }

  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Spy class without default constructor with Mockito API does not work")
  fun `Spy class without default constructor with Mockito API does not work`() {
    `when`
    Mockito.spy(ClassWithoutDefaultConstructor::class.java)

    then
    thrown(MockitoException::class.java)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Class is not mockable sample for asking org.mockito.plugins.MockMaker.isTypeMockable")
  fun `Class is not mockable sample for asking org mockito plugins MockMaker isTypeMockable`() {
    given
    val expectedMessage = if (Jvm.getCurrent().isJava8) {
      "Cannot create mock for class java.lang.Class. mockito: Cannot mock wrapper types, String.class or Class.class"
    } else {
      "Cannot create mock for class java.lang.Class. " +
        "mockito: Cannot mock primitive wrapper types, String, Class, or WeakReference"
    }

    `when`
    Mock(mapOf("mockMaker" to mockito), Class::class.java)

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.message == expectedMessage
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#mockito mock setting without mockito settings shall not fail")
  fun `mockito mock setting without mockito settings shall not fail`() {
    `when`
    val mock: Runnable = Mock(mapOf("mockMaker" to mockito(settings { })))

    then
    mock != null
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Mocking interface from different classloader works with mockito MockMaker")
  fun `Mocking interface from different classloader works with mockito MockMaker`() {
    given
    val mockUtil = MockUtil()
    val interfaceClass = ByteBuddyTestClassLoader().defineInterface("Interface")

    `when`
    val m = Mock(mapOf("mockMaker" to mockito), interfaceClass)

    then
    interfaceClass.isInstance(m)
    mockUtil.isMock(m)
  }

  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Mocking interface from different classloader works with default MockMaker")
  fun `Mocking interface from different classloader works with default MockMaker`() {
    given
    val mockUtil = MockUtil()
    val interfaceClass = ByteBuddyTestClassLoader().defineInterface("Interface")

    `when`
    val m = Mock(interfaceClass)

    then
    interfaceClass.isInstance(m)
    mockUtil.isMock(m)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoMockMakerSpec#Mocking with additional interface from different classloader works with mockito MockMaker")
  fun `Mocking with additional interface from different classloader works with mockito MockMaker`() {
    given
    val mockUtil = MockUtil()
    val additionalInterfaceClass = ByteBuddyTestClassLoader().defineInterface("AdditionalInterface1")

    `when`
    val m: Runnable = Mock(mapOf("mockMaker" to mockito, "additionalInterfaces" to listOf(additionalInterfaceClass)))

    then
    m is Runnable
    additionalInterfaceClass.isInstance(m)
    mockUtil.isMock(m)
  }

  private fun settings(block: MockSettings.() -> Unit): Closure<Any?> = object : Closure<Any?>(null) {
    @Suppress("unused")
    fun doCall(settings: MockSettings) {
      settings.block()
    }
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

open class ClassWithFinalMethod {
  fun finalMethod(): String = "realValue"
}

open class ClassThrowingException {
  open fun throwsException(): Unit = throw UnsupportedOperationException("TestEx")
}

enum class TestEnum {
  A,
  B;

  fun method(): String = toString()
}

open class AccessProtectedBaseClass {
  protected var nonStaticFlag: Boolean = true

  companion object {
    @JvmStatic
    protected var staticFlag: Boolean = true
  }
}

open class AccessProtectedSubClass : AccessProtectedBaseClass() {
  open fun accessNonStaticFlag(): Boolean = nonStaticFlag

  open fun accessStaticFlag(): Boolean = staticFlag
}

open class DataClass(open val bool: Boolean, open val string: String?) {
  constructor() : this(true, "data")
}

open class ClassWithoutDefaultConstructor(@Suppress("UNUSED_PARAMETER") arg: String)
