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
import io.github.pshevche.spockk.lang.Stub
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import net.bytebuddy.ByteBuddy
import org.spockframework.mock.runtime.IProxyBasedMockInterceptor
import org.spockframework.mock.runtime.MockCreationSettings
import spock.lang.Isolated
import spock.lang.Specification
import spock.lang.Timeout
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Phaser
import java.util.concurrent.TimeUnit

@Isolated("Run isolated, to prevent the test to be flaky under load")
class ByteBuddyMockFactoryConcurrentSpec : Specification() {

  @MigratedFrom("org.spockframework.mock.runtime.ByteBuddyMockFactoryConcurrentSpec#ensure lockMask bit patterns")
  fun `ensure lockMask bit patterns`() {
    given
    val lockMask = lockMask()

    expect
    1 shl (Integer.bitCount(lockMask) - 1) == Integer.highestOneBit(lockMask)
  }

  // Just to be safe to abort, normally the tests run in 2 secs.
  @Timeout(120)
  @MigratedFrom("org.spockframework.mock.runtime.ByteBuddyMockFactoryConcurrentSpec#cacheLockingStressTest #test")
  fun `cacheLockingStressTest #test`(test: String, mockSpecA: MockSpec, mockSpecB: MockSpec) {
    given
    val iterations = 5_000
    val tempClassLoader = ByteBuddyTestClassLoader()
    val featA = toMockFeatures(mockSpecA, tempClassLoader)
    val featB = toMockFeatures(mockSpecB, tempClassLoader)
    val interceptor = Stub(IProxyBasedMockInterceptor::class.java)
    val mockFactory = newMockFactory()
    val phaser = Phaser(4)
    val runCode = { code: Runnable ->
      CompletableFuture.runAsync {
        phaser.arriveAndAwaitAdvance()
        try {
          repeat(iterations) { code.run() }
        } finally {
          phaser.arrive()
        }
      }
    }

    `when`
    val mockFeatAFuture = runCode(
      Runnable {
        val mockClass = mockClass(mockFactory, tempClassLoader, featA, interceptor)
        assertValidMockClass(featA, mockClass, tempClassLoader)
      }
    )
    val mockFeatBFuture = runCode(
      Runnable {
        val mockClass = mockClass(mockFactory, tempClassLoader, featB, interceptor)
        assertValidMockClass(featB, mockClass, tempClassLoader)
      }
    )
    val cacheFuture = runCode(Runnable { clearCache(mockFactory) })

    phaser.arriveAndAwaitAdvance()
    val phase = phaser.arrive()
    try {
      phaser.awaitAdvanceInterruptibly(phase, 30, TimeUnit.SECONDS)
    } finally {
      // Collect exceptions from the futures, to make issues visible.
      mockFeatAFuture.getNow(null)
      mockFeatBFuture.getNow(null)
      cacheFuture.getNow(null)
    }

    then
    noExceptionThrown()

    where
    test                                      ; mockSpecA                  ; mockSpecB
    "same hashcode different mockType"        ; mockSpec(IF_A, IF_B)       ; mockSpec(IF_B, IF_A)
    "same hashcode same mockType"             ; mockSpec(IF_A)             ; mockSpec(IF_A)
    "different hashcode different interfaces" ; mockSpec(IF_A, IF_B)       ; mockSpec(IF_B, IF_C)
    "unrelated classes"                       ; mockSpec(IF_A)             ; mockSpec(IF_B)
  }

  private fun mockClass(
    mockFactory: Any,
    classLoader: ClassLoader,
    feature: MockFeatures,
    interceptor: IProxyBasedMockInterceptor
  ): Class<*> {
    val settings = MockCreationSettings.settings(feature.mockType, feature.interfaces, interceptor, classLoader, false)
    val createMock = mockFactory.javaClass.getDeclaredMethod("createMock", settingsType)
    createMock.isAccessible = true
    return createMock.invoke(mockFactory, settings).javaClass
  }

  private fun assertValidMockClass(feature: MockFeatures, mockClass: Class<*>, classLoader: ClassLoader) {
    check(mockClass.classLoader == classLoader) { "Mock class is not loaded by the expected class loader" }
    check(feature.mockType.isAssignableFrom(mockClass)) { "Mock class is not a subtype of ${feature.mockType}" }
    feature.interfaces.forEach {
      check(it.isAssignableFrom(mockClass)) { "Mock class does not implement $it" }
    }
  }

  private fun toMockFeatures(spec: MockSpec, classLoader: ByteBuddyTestClassLoader) = MockFeatures(
    classLoader.defineInterface(spec.mockType),
    spec.interfaces.map { classLoader.defineInterface(it) }
  )

  // ByteBuddyMockFactory and its cache internals are package-private in Spock, so they are reached by reflection.
  private fun factoryClass() = Class.forName("org.spockframework.mock.runtime.ByteBuddyMockFactory")

  private fun lockMask(): Int = factoryClass().getDeclaredField("CACHE_LOCK_MASK")
    .apply { isAccessible = true }
    .getInt(null)

  private fun newMockFactory(): Any = factoryClass().getDeclaredConstructor()
    .apply { isAccessible = true }
    .newInstance()

  private fun clearCache(mockFactory: Any) {
    val cache = mockFactory.javaClass.getDeclaredField("CACHE").apply { isAccessible = true }.get(mockFactory)
    cache.javaClass.getMethod("clear").invoke(cache)
  }

  class ByteBuddyTestClassLoader : ClassLoader() {
    private val cache = HashMap<String, Class<*>>()

    @Synchronized
    fun defineInterface(name: String): Class<*> = cache.getOrPut(name) {
      val bytes = ByteBuddy().makeInterface().name(name).make().bytes
      defineClass(name, bytes, 0, bytes.size)
    }
  }

  class MockFeatures(val mockType: Class<*>, val interfaces: List<Class<*>>)

  class MockSpec(val mockType: String, val interfaces: List<String>)

  companion object {
    private const val IF_A = "IfA"
    private const val IF_B = "IfB"
    private const val IF_C = "IfC"

    private val settingsType = Class.forName("org.spockframework.mock.runtime.IMockMaker\$IMockCreationSettings")

    private fun mockSpec(mockedType: String, vararg interfaces: String) = MockSpec(mockedType, interfaces.toList())
  }
}
