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

package io.github.pshevche.spockk.fixtures.runtime.samples.extension

import groovy.lang.Closure
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.where
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.extension.builtin.PreconditionContext
import spock.lang.FailsWith
import spock.lang.IgnoreIf
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification

/** Carries the per-run `shouldRun` value that upstream interpolates into the embedded spec source. */
object RequiresFlag {
  private val holder = ThreadLocal.withInitial { false }

  var shouldRun: Boolean
    get() = holder.get()
    set(value) = holder.set(value)
}

/** Stands in for upstream's `{ throw new RuntimeException() }.call()`: must never run when the extension skips the feature. */
fun failingDataProvider(): Int = throw RuntimeException("data provider must not run")

/** Base for condition closures: Spock calls them with the [PreconditionContext] as the `it` argument. */
abstract class Precondition(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  fun doCall(context: PreconditionContext<*>): Boolean = test(context)

  abstract fun test(context: PreconditionContext<*>): Boolean
}

class AlwaysTrue(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>) = 1 < 2
}

class AlwaysFalse(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>) = 1 > 2
}

// Fails during condition evaluation, before any data provider runs.
class ReferencesUnknownProperty(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>): Boolean = getProperty("b") as Boolean
}

class ReferencesUnknownDataVariable(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>): Boolean = context.data["b"] == true
}

class ThrowsArbitraryException(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>): Boolean = throw UnsupportedOperationException()
}

class UnknownPropertySpec : Specification() {
  @Requires(ReferencesUnknownProperty::class)
  fun foo(a: Int) {
    expect
    assert(false)

    where
    variable(a).from(failingDataProvider())
  }
}

class UnknownDataVariableSpec : Specification() {
  @Requires(ReferencesUnknownDataVariable::class)
  fun foo(a: Int) {
    expect
    assert(false)

    where
    variable(a).from(failingDataProvider())
  }
}

class ArbitraryExceptionSpec : Specification() {
  @Requires(ThrowsArbitraryException::class)
  fun foo(a: Int) {
    expect
    assert(false)

    where
    variable(a).from(failingDataProvider())
  }
}

@Requires(AlwaysTrue::class)
class SpecUsageWithTrue : Specification() {
  fun `basic usage`() {
    expect
    assert(true)
  }
}

class StaticMethodCondition(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>): Boolean = invokeMethod("shouldRun", emptyArray<Any?>()) as Boolean
}

@Requires(StaticMethodCondition::class)
class SpecUsageWithUnqualifiedStaticMethodAccess : Specification() {
  fun `basic usage`() {
    expect
    assert(true)
  }

  companion object {
    @JvmStatic fun shouldRun(): Boolean = RequiresFlag.shouldRun
  }
}

class StaticFieldCondition(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>): Boolean = getProperty("shouldRun") as Boolean
}

@Requires(StaticFieldCondition::class)
class SpecUsageWithUnqualifiedStaticFieldAccess : Specification() {
  fun `basic usage`() {
    expect
    assert(true)
  }

  companion object {
    @JvmStatic val shouldRun: Boolean
      get() = RequiresFlag.shouldRun
  }
}

class SharedFieldCondition(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>) = (context.shared as SpecUsageWithSharedFieldAccess).shouldRun
}

@Requires(SharedFieldCondition::class)
class SpecUsageWithSharedFieldAccess : Specification() {
  @Shared var shouldRun = RequiresFlag.shouldRun

  fun `basic usage`() {
    expect
    assert(true)
  }
}

class InstanceFieldCondition(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>) = (context.instance as SpecUsageWithInstanceFieldAccess).shouldRun
}

@Requires(InstanceFieldCondition::class)
class SpecUsageWithInstanceFieldAccess : Specification() {
  var shouldRun = RequiresFlag.shouldRun

  fun `basic usage`() {
    expect
    assert(true)
  }
}

@Requires(AlwaysFalse::class)
class SpecUsageWithFalse : Specification() {
  fun `basic usage`() {
    expect
    assert(false)
  }
}

@Requires(reason = "dummy message", value = NeverRuns::class)
class IgnoredSpecWithCustomMessage : Specification() {
  fun `basic usage`() {
    expect
    assert(false)
  }
}

class NeverRuns(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>) = false
}

// Spock instantiates a condition via its (owner, thisObject) constructor; this one has no such constructor.
class UninstantiableCondition(owner: Any?) : Closure<Boolean>(owner)

class UninstantiableConditionSpec : Specification() {
  @Requires(UninstantiableCondition::class)
  fun `basic usage`() {
    expect
    assert(false)
  }
}

class InstanceSharedValueCondition(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>) = (context.instance as InstanceSharedFieldAccessSpec).value != 2
}

class InstanceSharedFieldAccessSpec : Specification() {
  @Shared var value = 0

  @Requires(InstanceSharedValueCondition::class)
  fun `bar #input`(input: Int) {
    value = input

    expect
    assert(true)

    where
    variable(input).from(listOf(1, 2, 3))
  }
}

class SharedValueCondition(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>) = (context.shared as SharedFieldAccessSpec).value != 2
}

class SharedFieldAccessSpec : Specification() {
  @Shared var value = 0

  @Requires(SharedValueCondition::class)
  fun `bar #input`(input: Int) {
    value = input

    expect
    assert(true)

    where
    variable(input).from(listOf(1, 2, 3))
  }
}

class InstanceFieldValueCondition(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>) = (context.instance as InstanceFieldAccessSpec).value != 2
}

class InstanceFieldAccessSpec : Specification() {
  var value = 0

  fun setup() {
    value = staticValue
  }

  @Requires(InstanceFieldValueCondition::class)
  fun `bar #input`(input: Int) {
    staticValue = input

    expect
    assert(true)

    where
    variable(input).from(listOf(1, 2, 3))
  }

  companion object {
    @JvmStatic var staticValue = 0
  }
}

class StaticValueIsOne(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>) = getProperty("value") == 1
}

class StaticValueIsNotOne(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>) = getProperty("value") != 1
}

class StaticFieldAccessSpec : Specification() {
  @Requires(StaticValueIsOne::class)
  fun bar() {
    expect
    assert(true)
  }

  @Requires(StaticValueIsNotOne::class)
  fun baz() {
    expect
    assert(false)
  }

  companion object {
    @JvmStatic val value = 1
  }
}

open class InheritanceBase : Specification() {
  fun `base feature`() {
    expect
    assert(true)
  }
}

@Requires(value = NeverRuns::class, inherited = false)
open class NotInheritedFoo : InheritanceBase() {
  fun `foo feature`() {
    expect
    assert(true)
  }
}

open class NotInheritedBar : NotInheritedFoo() {
  fun `bar feature`() {
    expect
    assert(true)
  }
}

class NotInheritedTest : NotInheritedBar() {
  fun `test feature`() {
    expect
    assert(true)
  }
}

@Requires(value = NeverRuns::class, inherited = true)
open class InheritedFoo : InheritanceBase() {
  fun `foo feature`() {
    expect
    assert(true)
  }
}

open class InheritedBar : InheritedFoo() {
  fun `bar feature`() {
    expect
    assert(true)
  }
}

class InheritedTest : InheritedBar() {
  fun `test feature`() {
    expect
    assert(true)
  }
}

class OsInformation(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>) =
    context.os.isWindows || context.os.isLinux || context.os.isMacOs || context.os.isSolaris || context.os.isOther
}

class JvmInformation(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>): Boolean {
    val jvm = context.jvm
    return jvm.isJava8 || jvm.isJava9 || jvm.isJava10 || jvm.isJava11 || jvm.isJava12 || jvm.isJava13 ||
      jvm.isJava14 || jvm.isJava15 || jvm.isJava16 || jvm.isJava17 || jvm.isJavaVersion(18) ||
      jvm.isJava21 || jvm.isJava22 || jvm.isJava23 || jvm.isJava24 || jvm.isJava25 || jvm.isJava26 ||
      jvm.isJava27 || jvm.isJava28 || jvm.isJava29
  }
}

class EnvironmentVariables(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>) = !context.env.containsKey("FOO_BAR_BAZ")
}

class SystemProperties(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>) = context.sys.containsKey("java.version")
}

class DataVariableIsTwo(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>) = context.data["a"] == 2
}

class SharedShouldRun(owner: Any?, thisObject: Any?) : Precondition(owner, thisObject) {
  override fun test(context: PreconditionContext<*>) = (context.shared as RequiresExtensionExamples).shouldRun
}

class RequiresExtensionExamples : Specification() {
  @Shared var shouldRun = false

  @Requires(AlwaysTrue::class)
  fun `runs feature if precondition is satisfied`() {
    expect
    assert(true)
  }

  @Requires(AlwaysFalse::class)
  fun `skips feature if precondition is not satisfied`() {
    expect
    assert(true)
  }

  @Requires(OsInformation::class)
  fun `provides OS information`() {
    expect
    assert(true)
  }

  @Requires(JvmInformation::class)
  fun `provides JVM information`() {
    expect
    assert(true)
  }

  @Requires(EnvironmentVariables::class)
  fun `provides access to environment variables`() {
    expect
    assert(true)
  }

  @Requires(SystemProperties::class)
  fun `provides access to system properties`() {
    expect
    assert(true)
  }

  @Requires(SystemProperties::class)
  fun `can use closure argument for an easy option to typecast and use IDE support`() {
    expect
    assert(true)
  }

  @Requires(DataVariableIsTwo::class)
  fun `can evaluate for single iterations if data variables are accessed`(a: Int) {
    expect
    assert(a == 2)

    where
    variable(a).from(listOf(1, 2))
  }

  @Requires(NeverRuns::class)
  fun `can skip data providers completely if no data variables are accessed`(a: Int) {
    expect
    assert(false)

    where
    variable(a).from(failingDataProvider())
  }

  @Requires(SharedShouldRun::class)
  fun `can skip data providers completely if no data variables are accessed by checking a shared field`(a: Int) {
    expect
    assert(false)

    where
    variable(a).from(failingDataProvider())
  }

  @IgnoreIf(AlwaysTrue::class)
  @Requires(AlwaysTrue::class)
  fun `allows determinate use of multiple filters`() {
    expect
    assert(false)
  }

  @Requires(NeverRuns::class)
  @Requires(AlwaysTrue::class)
  fun `feature is ignored if at least one Requires annotation is false`() {
    expect
    assert(false)
  }

  @Requires(AlwaysTrue::class)
  @Requires(AlwaysTrue::class)
  @FailsWith(ConditionNotSatisfiedError::class)
  fun `feature is not ignored if all Requires annotations are true`() {
    expect
    assert(false)
  }
}
