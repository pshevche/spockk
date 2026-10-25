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

package io.github.pshevche.spockk.smoke

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.cleanup.CleanupBlocksLog
import io.github.pshevche.spockk.fixtures.runtime.samples.cleanup.CleanupBlocksLogSpec
import io.github.pshevche.spockk.lang.cleanup
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.setup
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod
import spock.lang.Specification

class CleanupBlocks : Specification() {

  private data class Primitives(
    val b: Byte,
    val i: Int,
    val l: Long,
    val f: Float,
    val d: Double,
    val bool: Boolean
  )

  @MigratedFrom("org.spockframework.smoke.CleanupBlocks#basic usage")
  fun `basic usage`() {
    setup
    var x = 1

    expect
    x == 1

    cleanup
    x = 0
  }

  @MigratedFrom("org.spockframework.smoke.CleanupBlocks#may access all previously defined local vars")
  fun `may access all previously defined local vars`() {
    setup
    var a: Int
    var b: Int
    `when`
    var c: String
    then
    var d: List<Int>?

    cleanup
    a = 0
    b = 0
    c = ""
    d = null
  }

  @MigratedFrom("org.spockframework.smoke.CleanupBlocks#is executed if exception is thrown")
  fun `is executed if exception is thrown`() {
    `when`
    CleanupBlocksLog.entries.clear()
    val events = execute(selectMethod(CleanupBlocksLogSpec::class.java, "feature throws"))

    then
    events.assertStatistics { it.started(1).failed(1) }
    val failure = events.failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
    failure is IllegalArgumentException
    CleanupBlocksLog.entries == listOf("feature", "cleanup")
  }

  @MigratedFrom("org.spockframework.smoke.CleanupBlocks#feature exception and cleanup exception are both reported")
  fun `feature exception and cleanup exception are both reported`() {
    `when`
    CleanupBlocksLog.entries.clear()
    val events = execute(selectMethod(CleanupBlocksLogSpec::class.java, "feature and cleanup throw"))

    then
    events.assertStatistics { it.started(1).failed(1) }
    val failure = events.failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
    failure.message == "feature"
    failure.suppressed[0].message == "cleanup"
    CleanupBlocksLog.entries == listOf("feature", "cleanup")
  }

  @MigratedFrom("org.spockframework.smoke.CleanupBlocks#variable with primitive type can be declared in presence of cleanup-block")
  fun `variable with primitive type can be declared in presence of cleanup-block`() {
    setup
    val x: Int = 1

    expect
    x == 1

    cleanup
    listOf<Any>()
  }

  @MigratedFrom("org.spockframework.smoke.CleanupBlocks#multi-declaration with primitive type in presence of cleanup-block")
  fun `multi-declaration with primitive type in presence of cleanup-block`() {
    `when`
    val (foo, bar) = Pair("foo", 42)

    then
    foo == "foo"
    bar == 42

    cleanup
    assert(foo == "foo")
    assert(bar == 42)
  }

  @MigratedFrom("org.spockframework.smoke.CleanupBlocks#multi-declaration with all primitive types in presence of cleanup-block")
  fun `multi-declaration with all primitive types in presence of cleanup-block`() {
    setup
    val (b, i, l, f, d, bool) = Primitives(1, 1, 1L, 1f, 1.0, true)

    expect
    b == 1.toByte()
    i == 1
    l == 1L
    f == 1f
    d == 1.0
    bool

    cleanup
    assert(b == 1.toByte())
    assert(i == 1)
    assert(l == 1L)
    assert(f == 1f)
    assert(d == 1.0)
    assert(bool)
  }

  @MigratedFrom(
    "org.spockframework.smoke.CleanupBlocks#cleanup blocks don't destroy method reference when invocation is assigned to variable with the same name"
  )
  fun `cleanup blocks don't destroy method reference when invocation is assigned to variable with the same name`() {
    `when`
    val foobar = foobar()

    then
    consume(foobar)

    cleanup
    foobar.size
  }

  @MigratedFrom(
    "org.spockframework.smoke.CleanupBlocks#cleanup blocks don't destroy method reference when invocation is assigned to a multi-assignment with the same name"
  )
  fun `cleanup blocks don't destroy method reference when invocation is assigned to a multi-assignment with the same name`() {
    `when`
    val (foobar, b) = foobar2()

    then
    consume(foobar)

    cleanup
    foobar.length
  }

  private fun consume(@Suppress("UNUSED_PARAMETER") value: Any) {}

  private fun foobar() = listOf("foo", "bar")

  private fun foobar2() = Pair("foo", "bar")
}
