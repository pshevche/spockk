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

package io.github.pshevche.spockk.smoke.extension

import groovy.lang.Closure
import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import org.spockframework.runtime.extension.builtin.PreconditionContext
import org.spockframework.runtime.model.parallel.ExecutionMode
import spock.lang.Execution
import spock.lang.IgnoreIf
import spock.lang.Specification

// Spock instantiates the @IgnoreIf closure class reflectively with (owner, thisObject).
abstract class PreconditionClosure(owner: Any?, thisObject: Any?) : Closure<Any?>(owner, thisObject)

class AlwaysTrue(owner: Any?, thisObject: Any?) : PreconditionClosure(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = 1 < 2
}

class AlwaysFalse(owner: Any?, thisObject: Any?) : PreconditionClosure(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = 1 > 2
}

class NonEmptyList(owner: Any?, thisObject: Any?) : PreconditionClosure(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = listOf(1, 2, 3)
}

class EmptyList(owner: Any?, thisObject: Any?) : PreconditionClosure(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = emptyList<Int>()
}

class OldJavaVersion(owner: Any?, thisObject: Any?) : PreconditionClosure(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = context.javaVersion < java.math.BigDecimal("1.5")
}

class EnvMismatch(owner: Any?, thisObject: Any?) : PreconditionClosure(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = context.let { it.env["PATH"] != System.getenv("PATH") }
}

class SysMismatch(owner: Any?, thisObject: Any?) : PreconditionClosure(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = context.let { it.sys["os.name"] != System.getProperty("os.name") }
}

@Execution(ExecutionMode.SAME_THREAD)
class ConditionallyIgnoreFeature : Specification() {

  companion object {
    @JvmStatic
    val log = mutableListOf<Int>()
  }

  @MigratedFrom("org.spockframework.smoke.extension.ConditionallyIgnoreFeature#should be ignored")
  @IgnoreIf(AlwaysTrue::class)
  fun `should be ignored`() {
    log += 1
    expect
    false
  }

  @MigratedFrom("org.spockframework.smoke.extension.ConditionallyIgnoreFeature#should be run")
  @IgnoreIf(AlwaysFalse::class)
  fun `should be run`() {
    log += 2
    expect
    true
  }

  @MigratedFrom("org.spockframework.smoke.extension.ConditionallyIgnoreFeature#should be ignored according to Groovy truth")
  @IgnoreIf(NonEmptyList::class)
  fun `should be ignored according to Groovy truth`() {
    log += 3
    expect
    false
  }

  @MigratedFrom("org.spockframework.smoke.extension.ConditionallyIgnoreFeature#should be run according to Groovy truth")
  @IgnoreIf(EmptyList::class)
  fun `should be run according to Groovy truth`() {
    log += 4
    expect
    true
  }

  @MigratedFrom("org.spockframework.smoke.extension.ConditionallyIgnoreFeature#provides convenient access to Java version")
  @IgnoreIf(OldJavaVersion::class)
  fun `provides convenient access to Java version`() {
    log += 5
    expect
    true
  }

  @MigratedFrom("org.spockframework.smoke.extension.ConditionallyIgnoreFeature#provides convenient access to environment variables")
  @IgnoreIf(EnvMismatch::class)
  fun `provides convenient access to environment variables`() {
    log += 6
    expect
    true
  }

  @MigratedFrom("org.spockframework.smoke.extension.ConditionallyIgnoreFeature#provides convenient access to system properties")
  @IgnoreIf(SysMismatch::class)
  fun `provides convenient access to system properties`() {
    log += 7
    expect
    true
  }

  fun cleanupSpec() {
    check(log.toSet() == setOf(2, 4, 5, 6, 7)) { "unexpected features ran: $log" }
  }
}
