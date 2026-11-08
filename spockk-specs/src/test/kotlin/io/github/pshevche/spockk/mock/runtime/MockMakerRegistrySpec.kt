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
import io.github.pshevche.spockk.lang.Mock
import io.github.pshevche.spockk.lang.Spy
import io.github.pshevche.spockk.lang.Stub
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.variables
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.spockframework.mock.CannotCreateMockException
import org.spockframework.mock.IMockObject
import org.spockframework.mock.runtime.ByteBuddyMockMaker
import org.spockframework.mock.runtime.CglibMockMaker
import org.spockframework.mock.runtime.IMockMaker
import org.spockframework.mock.runtime.IMockMaker.IMockCreationSettings
import org.spockframework.mock.runtime.IMockMaker.IMockabilityResult
import org.spockframework.mock.runtime.IMockMaker.MockMakerCapability
import org.spockframework.mock.runtime.JavaProxyMockMaker
import org.spockframework.mock.runtime.MockMakerRegistry
import org.spockframework.mock.runtime.mockito.MockitoMockMaker
import org.spockframework.runtime.RunContext
import org.spockframework.util.InternalSpockError
import spock.lang.PendingFeature
import spock.lang.Shared
import spock.lang.Specification
import spock.mock.IMockMakerSettings
import spock.mock.MockMakerId
import spock.mock.MockMakers
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.Callable

class MockMakerRegistrySpec : Specification() {

  companion object {
    private val TEST_ID = MockMakerId("test")
    private const val TEST_PRIORITY = 2

    // The registry constructor is package-private in Spock, so reach it reflectively and rethrow the real cause.
    private fun registryOf(makers: List<IMockMaker>, preferred: IMockMakerSettings?): MockMakerRegistry {
      val constructor = MockMakerRegistry::class.java.getDeclaredConstructor(
        Iterable::class.java,
        IMockMakerSettings::class.java
      )
      constructor.isAccessible = true
      try {
        return constructor.newInstance(makers, preferred)
      } catch (e: InvocationTargetException) {
        throw e.targetException
      }
    }
  }

  @Shared
  private val defaultMockMakerRegistry: MockMakerRegistry = RunContext.get().mockMakerRegistry

  @MigratedFrom("org.spockframework.mock.runtime.MockMakerRegistrySpec#No MockMaker")
  fun `No MockMaker`() {
    `when`
    registryOf(emptyList(), null)

    then
    val ex = thrown(InternalSpockError::class.java)
    ex.message == "No IMockMaker implementations found."
  }

  @MigratedFrom("org.spockframework.mock.runtime.MockMakerRegistrySpec#Invalid id: null")
  fun `Invalid id null`() {
    given
    val maker = Mock(IMockMaker::class.java)

    `when`
    registryOf(listOf(maker), null)

    then
    val ex = thrown(IllegalStateException::class.java)
    ex.message!!.endsWith(" has an invalid ID. The ID must not be null.")
  }

  @MigratedFrom("org.spockframework.mock.runtime.MockMakerRegistrySpec#Invalid priority")
  fun `Invalid priority`(inputPriority: Int, expectedErrorMessage: String) {
    given
    val maker = Mock(IMockMaker::class.java) {
      getId() returns TEST_ID
      getPriority() returns inputPriority
      getCapabilities() returns setOf(MockMakerCapability.INTERFACE)
    }

    `when`
    registryOf(listOf(maker), null)

    then
    val ex = thrown(IllegalStateException::class.java)
    ex.message == expectedErrorMessage

    where
    variables(inputPriority, expectedErrorMessage).from(
      listOf(-1, 0),
      listOf(
        "The IMockMaker '$TEST_ID' has an invalid priority -1. Priorities < 0 are invalid.",
        "The IMockMaker '$TEST_ID' has an invalid priority 0. The priority 0 is reserved."
      )
    )
  }

  @MigratedFrom("org.spockframework.mock.runtime.MockMakerRegistrySpec#No capabilities - null")
  fun `No capabilities - null`() {
    given
    val maker = Mock(IMockMaker::class.java) {
      getId() returns TEST_ID
      getPriority() returns 5
    }

    `when`
    registryOf(listOf(maker), null)

    then
    val ex = thrown(IllegalStateException::class.java)
    ex.message == "The IMockMaker 'Mock for type 'IMockMaker' named 'maker'' does not define any capability."
  }

  @MigratedFrom("org.spockframework.mock.runtime.MockMakerRegistrySpec#No capabilities - empty")
  fun `No capabilities - empty`() {
    given
    val maker = Mock(IMockMaker::class.java) {
      getId() returns TEST_ID
      getPriority() returns 5
      getCapabilities() returns emptySet<MockMakerCapability>()
    }

    `when`
    registryOf(listOf(maker), null)

    then
    val ex = thrown(IllegalStateException::class.java)
    ex.message == "The IMockMaker 'Mock for type 'IMockMaker' named 'maker'' does not define any capability."
  }

  @MigratedFrom("org.spockframework.mock.runtime.MockMakerRegistrySpec#Duplicate mock makers")
  fun `Duplicate mock makers`() {
    given
    val maker1 = Mock(IMockMaker::class.java) {
      getId() returns TEST_ID
      getPriority() returns TEST_PRIORITY
      getCapabilities() returns setOf(MockMakerCapability.INTERFACE)
    }
    val maker2 = Mock(IMockMaker::class.java) {
      getId() returns TEST_ID
      getPriority() returns TEST_PRIORITY
      getCapabilities() returns setOf(MockMakerCapability.INTERFACE)
    }

    `when`
    registryOf(listOf(maker1, maker2), null)

    then
    val ex = thrown(IllegalStateException::class.java)
    ex.message == "Duplicated IMockMaker instances with the same ID found: $TEST_ID"
  }

  @MigratedFrom("org.spockframework.mock.runtime.MockMakerRegistrySpec#MockMaker sorting")
  fun `MockMaker sorting`() {
    given
    val samePriorityId = MockMakerId("same-priority")
    val proxy = JavaProxyMockMaker()
    val mockSamePriorityProxy = Mock(IMockMaker::class.java) {
      getId() returns samePriorityId
      getPriority() returns 200
      getCapabilities() returns setOf(MockMakerCapability.INTERFACE)
    }
    val byteBuddy = ByteBuddyMockMaker()
    val sampleMockMaker = Mock(IMockMaker::class.java) {
      getId() returns TEST_ID
      getPriority() returns 5
      getCapabilities() returns setOf(MockMakerCapability.INTERFACE)
    }
    val cglib = CglibMockMaker()

    `when`
    val r = registryOf(listOf(mockSamePriorityProxy, byteBuddy, cglib, sampleMockMaker, proxy), null)

    then
    r.makerList == listOf(sampleMockMaker, proxy, byteBuddy, mockSamePriorityProxy, cglib)
  }

  @MigratedFrom("org.spockframework.mock.runtime.MockMakerRegistrySpec#MockMaker sorting with preferred")
  fun `MockMaker sorting with preferred`() {
    given
    val samePriorityId = MockMakerId("same-priority")
    val proxy = JavaProxyMockMaker()
    val mockSamePriorityProxy = Mock(IMockMaker::class.java) {
      getId() returns samePriorityId
      getPriority() returns 200
      getCapabilities() returns setOf(MockMakerCapability.INTERFACE)
    }
    val byteBuddy = ByteBuddyMockMaker()
    val sampleMockMaker = Mock(IMockMaker::class.java) {
      getId() returns TEST_ID
      getPriority() returns 250
      getCapabilities() returns setOf(MockMakerCapability.INTERFACE)
    }
    val cglib = CglibMockMaker()

    `when`
    val r = registryOf(
      listOf(mockSamePriorityProxy, byteBuddy, cglib, sampleMockMaker, proxy),
      IMockMakerSettings.settingsFor(TEST_ID)
    )

    then
    r.makerList == listOf(sampleMockMaker, proxy, byteBuddy, mockSamePriorityProxy, cglib)
  }

  @MigratedFrom("org.spockframework.mock.runtime.MockMakerRegistrySpec#MockMaker sorting with preferred cglib")
  fun `MockMaker sorting with preferred cglib`() {
    given
    val proxy = JavaProxyMockMaker()
    val byteBuddy = ByteBuddyMockMaker()
    val cglib = CglibMockMaker()

    `when`
    val r = registryOf(listOf(byteBuddy, cglib, proxy), MockMakers.cglib)

    then
    r.makerList == listOf(cglib, proxy, byteBuddy)
  }

  @MigratedFrom("org.spockframework.mock.runtime.MockMakerRegistrySpec#MockMaker non existing preferred ID")
  fun `MockMaker non existing preferred ID`() {
    `when`
    registryOf(listOf(JavaProxyMockMaker()), MockMakers.cglib)

    then
    val ex = thrown(IllegalStateException::class.java)
    ex.message == "No IMockMaker with ID 'cglib' exists, but was request via mockMaker.preferredMockMaker " +
      "configuration. Is a runtime dependency missing?"
  }

  @MigratedFrom("org.spockframework.mock.runtime.MockMakerRegistrySpec#MockMaker loaded from ServiceLoader")
  fun `MockMaker loaded from ServiceLoader`() {
    `when`
    val makers = defaultMockMakerRegistry.makerList

    then
    makers[0] is JavaProxyMockMaker
    makers[1] is ByteBuddyMockMaker
    makers[2] is CglibMockMaker
    makers[3] is MockitoMockMaker
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.mock.runtime.MockMakerRegistrySpec#Mock with unknown MockMaker")
  fun `Mock with unknown MockMaker`() {
    `when`
    Mock(
      mapOf("mockMaker" to IMockMakerSettings.settingsFor(MockMakerId("unknown"))),
      Runnable::class.java
    )

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.message == "Cannot create mock for interface java.lang.Runnable because MockMaker with ID 'unknown' does not exist."
  }

  @MigratedFrom("org.spockframework.mock.runtime.MockMakerRegistrySpec#asMockOrNull() asks all IMockMakers")
  fun `asMockOrNull() asks all IMockMakers`() {
    given
    val maker = Mock(IMockMaker::class.java) {
      getId() returns MockMakers.byteBuddy.mockMakerId
      getPriority() returns 2
      getCapabilities() returns setOf(MockMakerCapability.INTERFACE)
    }
    val mockObj = Mock(IMockObject::class.java)
    val registry = registryOf(listOf(maker), null)
    val obj = "mockObj"

    `when`
    registry.asMockOrNull(obj)

    then
    1 * maker.asMockOrNull(obj) returns mockObj
  }

  @MigratedFrom("org.spockframework.mock.runtime.MockMakerRegistrySpec#Test the default IMockabilityResult.MOCKABLE")
  fun `Test the default IMockabilityResult MOCKABLE`() {
    expect
    IMockabilityResult.MOCKABLE.isMockable
    IMockabilityResult.MOCKABLE.notMockableReason == ""
  }

  @MigratedFrom("org.spockframework.mock.runtime.MockMakerRegistrySpec#Static Mock settings calls makeMock shall fail")
  fun `Static Mock settings calls makeMock shall fail`() {
    given
    val settings = Mock(IMockCreationSettings::class.java) {
      isStaticMock() returns true
    }

    `when`
    defaultMockMakerRegistry.makeMock(settings)

    then
    val ex = thrown(IllegalArgumentException::class.java)
    ex.message == "Can't mock static mocks with makeMock()."
  }

  @MigratedFrom(
    "org.spockframework.mock.runtime.MockMakerRegistrySpec#Non-static Mock settings calls makeStaticMock shall fail"
  )
  fun `Non-static Mock settings calls makeStaticMock shall fail`() {
    given
    val settings = Mock(IMockCreationSettings::class.java)

    `when`
    defaultMockMakerRegistry.makeStaticMock(settings)

    then
    val ex = thrown(IllegalArgumentException::class.java)
    ex.message == "Can't mock instance mocks with makeStaticMock()."
  }

  @MigratedFrom("org.spockframework.mock.runtime.MockMakerRegistrySpec#makeStaticMock")
  fun makeStaticMock() {
    given
    val mockMaker = Mock(IMockMaker::class.java) {
      getId() returns TEST_ID
      getPriority() returns TEST_PRIORITY
      getMockability(any()) returns IMockabilityResult.MOCKABLE
      getCapabilities() returns setOf(MockMakerCapability.CLASS, MockMakerCapability.STATIC_METHOD)
    }
    val settings = Mock(IMockCreationSettings::class.java) {
      getMockType() returns java.io.File::class.java
      getAdditionalInterface() returns emptyList<Class<*>>()
      isStaticMock() returns true
    }
    val registry = registryOf(listOf(mockMaker), null)

    `when`
    val staticMock = registry.makeStaticMock(settings)

    then
    1 * mockMaker.makeStaticMock(settings) returns Mock(IMockMaker.IStaticMock::class.java)
    staticMock != null
  }

  @MigratedFrom("org.spockframework.mock.runtime.MockMakerRegistrySpec#makeMock final class")
  fun `makeMock final class`() {
    given
    val mockMaker = Mock(IMockMaker::class.java) {
      getId() returns TEST_ID
      getPriority() returns TEST_PRIORITY
      getMockability(any()) returns IMockabilityResult.MOCKABLE
      getCapabilities() returns setOf(MockMakerCapability.CLASS, MockMakerCapability.FINAL_CLASS)
    }
    val settings = Mock(IMockCreationSettings::class.java) {
      getMockType() returns StringBuilder::class.java
      getAdditionalInterface() returns emptyList<Class<*>>()
    }
    val registry = registryOf(listOf(mockMaker), null)

    `when`
    val mock = registry.makeMock(settings)

    then
    1 * mockMaker.makeMock(settings) returns StringBuilder()
    mock is StringBuilder
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/935")
  @MigratedFrom(
    "org.spockframework.mock.runtime.MockMakerRegistrySpec#Default MockMakerImpl throws UnsupportedOperationException on makeStaticMock"
  )
  fun `Default MockMakerImpl throws UnsupportedOperationException on makeStaticMock`() {
    given
    val maker = Spy(IMockMaker::class.java)
    maker.getId() returns TEST_ID

    `when`
    maker.makeStaticMock(Stub(IMockCreationSettings::class.java))

    then
    val ex = thrown(UnsupportedOperationException::class.java)
    ex.message == "Operation not implemented by test, but claimed capability MockMakerCapability.STATIC_METHOD. " +
      "Settings: Mock for type 'IMockCreationSettings'"
  }

  @MigratedFrom(
    "org.spockframework.mock.runtime.MockMakerRegistrySpec#Mock throws CannotCreateMockException on capability INTERFACE"
  )
  fun `Mock throws CannotCreateMockException on capability INTERFACE`() {
    given
    val mockMaker = Mock(IMockMaker::class.java) {
      getId() returns TEST_ID
      getPriority() returns TEST_PRIORITY
      getCapabilities() returns setOf(MockMakerCapability.CLASS)
    }
    val settings = Mock(IMockCreationSettings::class.java) {
      getMockType() returns Runnable::class.java
      getAdditionalInterface() returns emptyList<Class<*>>()
    }
    val registry = registryOf(listOf(mockMaker), null)

    `when`
    registry.makeMock(settings)

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.message!!.contains("$TEST_ID: Cannot mock interfaces.")
  }

  @MigratedFrom(
    "org.spockframework.mock.runtime.MockMakerRegistrySpec#Mock throws CannotCreateMockException on capability ADDITIONAL_INTERFACES"
  )
  fun `Mock throws CannotCreateMockException on capability ADDITIONAL_INTERFACES`() {
    given
    val mockMaker = Mock(IMockMaker::class.java) {
      getId() returns TEST_ID
      getPriority() returns TEST_PRIORITY
      getCapabilities() returns setOf(MockMakerCapability.INTERFACE)
    }
    val settings = Mock(IMockCreationSettings::class.java) {
      getMockType() returns Runnable::class.java
      getAdditionalInterface() returns listOf<Class<*>>(Callable::class.java)
    }
    val registry = registryOf(listOf(mockMaker), null)

    `when`
    registry.makeMock(settings)

    then
    val ex = thrown(CannotCreateMockException::class.java)
    ex.message!!.contains("$TEST_ID: Cannot mock classes with additional interfaces")
  }
}
