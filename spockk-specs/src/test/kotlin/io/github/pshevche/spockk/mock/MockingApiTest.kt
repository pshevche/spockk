/*
 * Copyright 2025 the original author or authors.
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

package io.github.pshevche.spockk.mock

import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.jupiter.api.assertNotNull
import org.spockframework.mock.MockUtil
import spock.lang.Specification
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Tests the usage of the underlying Spock MockingApi is working. */
class MockingApiTest : Specification() {

  fun `Simple Mock interface with Java Class`() {
    `when`
    val m = Mock(Runnable::class.java)

    then
    assertNotNull(m)

    `when`
    m.run()

    then
    assertIsSpockMock(m)
    assertMockName(m, "m")
  }

  fun `Mock interface without variable`() {
    given
    Mock(Runnable::class.java)

    expect
    noExceptionThrown()
  }

  fun `Mock interface from variable type`() {
    given
    val m: Runnable = Mock()

    `when`
    m.run()
    then

    assertIsSpockMock(m)
    assertMockName(m, "m")
  }

  fun `MockName uses the variable name`() {
    `when`
    val myMock = Mock(Runnable::class.java)

    then
    assertIsSpockMock(myMock)
    assertMockName(myMock, "myMock")
  }

  fun `MockName uses the variable name and var type`() {
    `when`
    val myMock: Runnable = Mock()

    then
    assertIsSpockMock(myMock)
    assertMockName(myMock, "myMock")
  }

  fun `Simple Stub interface with Java Class`() {
    `when`
    val m = Stub(Runnable::class.java)

    then
    assertNotNull(m)

    `when`
    m.run()

    then
    assertIsSpockMock(m)
  }

  fun `Stub interface from variable type`() {
    given
    val m: Runnable = Stub()

    `when`
    m.run()

    then
    assertIsSpockMock(m)
  }

  fun `Simple Spy instance with Java Class`() {
    `when`
    val m = Spy(StringBuilder::class.java)

    then
    assertNotNull(m)

    `when`
    m.append("a")

    then
    assertEquals("a", m.toString())
    assertIsSpockMock(m)
  }

  fun `Spy instance from variable type`() {
    given
    val m: StringBuilder = Spy()

    `when`
    m.append("a")

    then
    assertEquals("a", m.toString())
    assertIsSpockMock(m)
  }

  fun mockInHelperMethod(): Runnable = Mock(Runnable::class.java)

  fun `Usage in MockingAPI in helper method`() {
    given
    val m = mockInHelperMethod()

    `when`
    m.run()

    then
    assertIsSpockMock(m)
  }

  val mockField = Mock(Runnable::class.java)!!

  fun `Usage in MockingAPI during field initialization`() {
    `when`
    mockField.run()

    then
    assertIsSpockMock(mockField)
  }

  private fun assertIsSpockMock(m: Any?) {
    assertTrue(MockUtil().isMock(m))
  }

  private fun assertMockName(m: Any?, name: String) {
    assertEquals(MockUtil().asMock(m)!!.name, name)
  }
}
