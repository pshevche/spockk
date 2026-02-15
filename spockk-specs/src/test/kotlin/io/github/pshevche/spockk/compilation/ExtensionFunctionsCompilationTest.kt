package io.github.pshevche.spockk.compilation

import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import io.github.pshevche.spockk.fixtures.compilation.CompilationUtils.transform
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import spock.lang.Issue
import kotlin.test.assertTrue

class ExtensionFunctionsCompilationTest : BaseCompilationTest() {

  @Issue("https://github.com/pshevche/spockk/issues/163")
  fun `handles extension functions in fixtures`() {
    given
    val fixture = kotlin(
      "Human.kt",
      """
          data class Human(val name: String)

          fun Human.greet(): String = "Hello from " + this.name
          """
        .trimIndent()
    )

    `when`
    val result = transform(fixture)

    then
    assertTrue(result.isSuccess())
  }
}
