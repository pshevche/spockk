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

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.and
import io.github.pshevche.spockk.lang.anyMethod
import io.github.pshevche.spockk.lang.cleanup
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import org.mockito.Answers
import org.mockito.Mockito
import org.mockito.exceptions.base.MockitoException
import org.spockframework.mock.CannotCreateMockException
import org.spockframework.mock.MockUtil
import org.spockframework.runtime.InvalidSpecException
import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.mock.IMockMakerSettings
import spock.mock.MockMakers
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class MockitoStaticMocksSpec : Specification() {

  private val mockUtil = MockUtil()

  fun cleanup() {
    check(!mockUtil.isStaticMock(StaticClass::class.java)) { "StaticClass static mock was not cleaned up" }
    check(!mockUtil.isStaticMock(StaticClass2::class.java)) { "StaticClass2 static mock was not cleaned up" }
  }

  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#Mockito API Test static mock with response")
  fun `Mockito API Test static mock with response`() {
    given
    val mock = Mockito.mockStatic(StaticClass::class.java)
    mock.`when`<String> { StaticClass.staticMethod() }.thenReturn(MOCK_VALUE)

    `when`
    val result = StaticClass.staticMethod()

    then
    result == MOCK_VALUE
    mock.verify { StaticClass.staticMethod() }

    and
    !mockUtil.isStaticMock(StaticClass::class.java)

    cleanup
    mock.close()
  }

  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#Mockito API Test static mock with no response defined")
  fun `Mockito API Test static mock with no response defined`() {
    given
    val mock = Mockito.mockStatic(StaticClass::class.java)

    `when`
    val result = StaticClass.staticMethod()

    then
    result == null
    mock.verify { StaticClass.staticMethod() }

    cleanup
    mock.close()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#Mockito and Spock API Test static mock on same class Mockito first")
  fun `Mockito and Spock API Test static mock on same class Mockito first`() {
    given
    val mock = Mockito.mockStatic(StaticClass::class.java)
    mock.`when`<String> { StaticClass.staticMethod() }.thenReturn(MOCK_VALUE)

    `when`
    SpyStatic(StaticClass::class.java)

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.message!!.contains("static mocking is already registered in the current thread")

    cleanup
    mock.close()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#Mockito and Spock API Test static mock on same class inverse Spock first")
  fun `Mockito and Spock API Test static mock on same class inverse Spock first`() {
    given
    SpyStatic(StaticClass::class.java)

    `when`
    Mockito.mockStatic(StaticClass::class.java)

    then
    val ex = thrown(MockitoException::class.java)
    ex.message!!.contains("static mocking is already registered in the current thread")
  }

  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#Mockito API Test static mock with default response defined")
  fun `Mockito API Test static mock with default response defined`() {
    given
    val mock = Mockito.mockStatic(StaticClass::class.java, Answers.CALLS_REAL_METHODS)

    `when`
    val result = StaticClass.staticMethod()

    then
    result == REAL_VALUE
    mock.verify { StaticClass.staticMethod() }

    cleanup
    mock.close()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#Spock API mocking same class twice shall fail")
  fun `Spock API mocking same class twice shall fail`() {
    given
    SpyStatic(StaticClass::class.java)

    `when`
    SpyStatic(StaticClass::class.java)

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.message!!.contains("static mocking is already registered in the current thread")
  }

  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#No mock")
  fun `No mock`() {
    expect
    StaticClass.staticMethod() == REAL_VALUE
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#Static mock for non-supporting mock-maker shall fail")
  fun `Static mock for non-supporting mock-maker shall fail`() {
    `when`
    SpyStatic(StaticClass::class.java, MockMakers.javaProxy)

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.message!!.endsWith("java-proxy: Cannot mock classes.")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#no static type specified")
  fun `no static type specified`() {
    `when`
    SpyStatic(null as Class<*>?)

    then
    val ex = thrown(InvalidSpecException::class.java)
    ex.message == "The type must not be null."
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#Invalid mockito static mock shall throw spock exception instead of Mockito exception")
  fun `Invalid mockito static mock shall throw spock exception instead of Mockito exception`() {
    `when`
    SpyStatic(Thread::class.java)

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.message!!.contains("It is not possible to mock static methods of java.lang.Thread")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#ClassCastException inside the getMockMakerId() from a non-static Spy is thrown as-is and is not processed by the SpyStatic(){} closure check")
  fun `ClassCastException inside the getMockMakerId() from a non-static Spy is thrown as-is and is not processed by the SpyStatic(){} closure check`() {
    `when`
    Spy(mapOf("mockMaker" to IMockMakerSettings { throw ClassCastException() }), Runnable::class.java)

    then
    thrown(ClassCastException::class.java)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#Mockito and Spock API Test static mock on different classes Mockito first")
  fun `Mockito and Spock API Test static mock on different classes Mockito first`() {
    given
    val mock = Mockito.mockStatic(StaticClass::class.java)
    mock.`when`<String> { StaticClass.staticMethod() }.thenReturn(MOCK_VALUE)

    SpyStatic(StaticClass2::class.java)
    StaticClass2.staticMethod() returns MOCK_VALUE2

    `when`
    var result = StaticClass.staticMethod()

    then
    result == MOCK_VALUE
    mock.verify { StaticClass.staticMethod() }

    `when`
    result = StaticClass2.staticMethod()

    then
    result == MOCK_VALUE2

    cleanup
    mock.close()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#Mockito and Spock API Test static mock on different classes Spock first")
  fun `Mockito and Spock API Test static mock on different classes Spock first`() {
    given
    SpyStatic(StaticClass2::class.java)
    StaticClass2.staticMethod() returns MOCK_VALUE2

    val mock = Mockito.mockStatic(StaticClass::class.java)
    mock.`when`<String> { StaticClass.staticMethod() }.thenReturn(MOCK_VALUE)

    `when`
    var result = StaticClass.staticMethod()

    then
    result == MOCK_VALUE
    mock.verify { StaticClass.staticMethod() }

    `when`
    result = StaticClass2.staticMethod()

    then
    result == MOCK_VALUE2

    cleanup
    mock.close()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#SpyStatic Spock API")
  fun `SpyStatic Spock API`() {
    given
    SpyStatic(StaticClass::class.java)

    `when`
    val result = StaticClass.staticMethod()

    then
    result == REAL_VALUE
    1 * StaticClass.staticMethod()
    mockUtil.isStaticMock(StaticClass::class.java)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#Spock API mocking two different classes")
  fun `Spock API mocking two different classes`() {
    given
    SpyStatic(StaticClass::class.java)
    StaticClass.staticMethod() returns MOCK_VALUE

    SpyStatic(StaticClass2::class.java)
    StaticClass2.staticMethod() returns MOCK_VALUE2

    expect
    StaticClass.staticMethod() == MOCK_VALUE
    StaticClass2.staticMethod() == MOCK_VALUE2
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#SpyStatic interactions in given")
  fun `SpyStatic interactions in given`() {
    given
    SpyStatic(StaticClass::class.java)
    StaticClass.staticMethod() returns MOCK_VALUE

    expect
    StaticClass.staticMethod() == MOCK_VALUE
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#SpyStatic interactions in when")
  fun `SpyStatic interactions in when`() {
    given
    SpyStatic(StaticClass::class.java)

    `when`
    StaticClass.staticMethod() returns MOCK_VALUE
    val result = StaticClass.staticMethod()

    then
    result == MOCK_VALUE
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#SpyStatic can activate the static mocks on different Thread")
  fun `SpyStatic can activate the static mocks on different Thread`() {
    given
    SpyStatic(StaticClass::class.java)
    StaticClass.staticMethod() returns MOCK_VALUE

    `when`
    val executor = Executors.newSingleThreadExecutor()
    val future = executor.submit(
      Callable {
        check(StaticClass.staticMethod() == REAL_VALUE)
        check(!mockUtil.isStaticMock(StaticClass::class.java))
        val callableResult = withActiveThreadAwareMocks(
          Callable {
            check(StaticClass.staticMethod() == MOCK_VALUE)
            check(mockUtil.isStaticMock(StaticClass::class.java))
            StaticClass.staticMethod()
          }
        )
        check(StaticClass.staticMethod() == REAL_VALUE)
        check(!mockUtil.isStaticMock(StaticClass::class.java))
        callableResult
      }
    )
    val result = future.get()

    then
    StaticClass.staticMethod() == MOCK_VALUE
    result == MOCK_VALUE
    mockUtil.isStaticMock(StaticClass::class.java)

    cleanup
    executor.shutdown()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#SpyStatic can activate two mocked static classes on different Thread")
  fun `SpyStatic can activate two mocked static classes on different Thread`() {
    given
    SpyStatic(StaticClass::class.java)
    StaticClass.staticMethod() returns MOCK_VALUE

    SpyStatic(StaticClass2::class.java)
    StaticClass2.staticMethod() returns MOCK_VALUE2

    `when`
    val executor = Executors.newSingleThreadExecutor()
    val future = executor.submit(
      Callable {
        check(StaticClass.staticMethod() == REAL_VALUE)
        check(StaticClass2.staticMethod() == REAL_VALUE)
        val callableResult = withActiveThreadAwareMocks(
          Callable {
            check(StaticClass.staticMethod() == MOCK_VALUE)
            check(StaticClass2.staticMethod() == MOCK_VALUE2)
            StaticClass.staticMethod()
          }
        )
        check(StaticClass.staticMethod() == REAL_VALUE)
        check(StaticClass2.staticMethod() == REAL_VALUE)
        callableResult
      }
    )
    val result = future.get()

    then
    StaticClass.staticMethod() == MOCK_VALUE
    StaticClass2.staticMethod() == MOCK_VALUE2
    result == MOCK_VALUE

    cleanup
    executor.shutdown()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#Static Mocks can be activated twice on the same thread")
  fun `Static Mocks can be activated twice on the same thread`() {
    given
    SpyStatic(StaticClass::class.java)
    StaticClass.staticMethod() returns MOCK_VALUE

    `when`
    runWithThreadAwareMocks { check(StaticClass.staticMethod() == MOCK_VALUE) }

    then
    StaticClass.staticMethod() == MOCK_VALUE
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#SpyStatic Spock API with response")
  fun `SpyStatic Spock API with response`() {
    given
    SpyStatic(StaticClass::class.java)

    `when`
    var result = StaticClass.staticMethod()

    then
    result == MOCK_VALUE
    1 * StaticClass.staticMethod() returns MOCK_VALUE

    `when`
    result = StaticClass.staticMethod()

    then
    result == REAL_VALUE
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#SpyStatic Spock API with closure response")
  fun `SpyStatic Spock API with closure response`() {
    given
    SpyStatic(StaticClass::class.java)
    StaticClass.staticMethod() returns MOCK_VALUE
    StaticClass.staticMethod2() returns MOCK_VALUE

    expect
    StaticClass.staticMethod() == MOCK_VALUE
    StaticClass.staticMethod2() == MOCK_VALUE
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#SpyStatic Spock API without response")
  fun `SpyStatic Spock API without response`() {
    given
    SpyStatic(StaticClass::class.java)

    `when`
    val result = StaticClass.staticMethod()

    then
    result == REAL_VALUE
    1 * StaticClass.staticMethod()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#SpyStatic with explicit mock-maker")
  fun `SpyStatic with explicit mock-maker`() {
    given
    SpyStatic(StaticClass::class.java, MockMakers.mockito)

    `when`
    val result = StaticClass.staticMethod()

    then
    1 * StaticClass.staticMethod() returns MOCK_VALUE
    0 * StaticClass.anyMethod()
    result == MOCK_VALUE
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#SpyStatic with explicit mock-maker with closure response")
  fun `SpyStatic with explicit mock-maker with closure response`() {
    given
    SpyStatic(StaticClass::class.java, MockMakers.mockito)
    StaticClass.staticMethod() returns MOCK_VALUE

    `when`
    val result = StaticClass.staticMethod()

    then
    result == MOCK_VALUE
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/710")
  @Issue("https://github.com/spockframework/spock/issues/2161")
  @MigratedFrom("org.spockframework.mock.runtime.mockito.MockitoStaticMocksSpec#SpyStatic with varargs in method")
  fun `SpyStatic with varargs in method`() {
    given
    SpyStatic(StaticClass::class.java)
    StaticClass.staticVarargsMethod("test") returns true
    StaticClass.staticVarargsMethod("test2") returns false

    expect
    StaticClass.staticVarargsMethod("test")
    !StaticClass.staticVarargsMethod("test2")
  }

  private companion object {
    const val REAL_VALUE = "RealValue"
    const val MOCK_VALUE = "MockValue"
    const val MOCK_VALUE2 = "MockValue2"
  }
}

object StaticClass {
  const val REAL_VALUE = "RealValue"

  @JvmStatic
  fun staticMethod(): String? = REAL_VALUE

  @JvmStatic
  fun staticMethod2(): String? = REAL_VALUE

  @JvmStatic
  fun staticVarargsMethod(str: String, vararg varargs: String): Boolean = true
}

object StaticClass2 {
  @JvmStatic
  fun staticMethod(): String? = StaticClass.REAL_VALUE
}
