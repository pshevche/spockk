package io.github.pshevche.spockk.smoke.mock

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
