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
import org.codehaus.groovy.runtime.InvokerHelper
import org.spockframework.runtime.extension.builtin.PreconditionContext
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification

// Spock instantiates the @IgnoreIf closure class reflectively with (owner, thisObject).
abstract class IgnoreIfCondition(owner: Any?, thisObject: Any?) : Closure<Any?>(owner, thisObject)

class IgnoreIfAlways(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = 1 < 2
}

class IgnoreIfNever(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = 1 > 2
}

class IgnoreIfTrue(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = true
}

class IgnoreIfFalse(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = false
}

class IgnoreIfAnyOs(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = context.os.let {
    it.isWindows || it.isLinux || it.isMacOs || it.isSolaris || it.isOther
  }
}

class IgnoreIfAnyJvm(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = context.jvm.let {
    it.isJava8 || it.isJava9 || it.isJava10 || it.isJava11 || it.isJava12 || it.isJava13 || it.isJava14 || it.isJava15 || it.isJava16 || it.isJava17 || it.isJava18 || it.isJava19 || it.isJava20 || it.isJava21 || it.isJava22 || it.isJava23 || it.isJava24 || it.isJava25
  }
}

class IgnoreIfEnvVarMissing(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = !context.env.containsKey("FOOBARBAZ")
}

class IgnoreIfSysPropPresent(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = !context.sys.contains("java.version")
}

class IgnoreIfDataAIsOne(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = context.data["a"] == 1
}

class IgnoreIfDataAIsNotOne(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = context.data["a"] != 1
}

class IgnoreIfUnknownProperty(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = InvokerHelper.getProperty(context, "b")
}

class IgnoreIfUnknownDataVariable(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = context.data["b"]
}

class IgnoreIfThrows(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = throw UnsupportedOperationException()
}

/** Lacks the (owner, thisObject) constructor, so Spock cannot instantiate it. */
class IgnoreIfUninstantiable(owner: Any?) : Closure<Any?>(owner)

private fun failingDataProvider(): Int = throw RuntimeException()

class IgnoreIfUnknownPropertySpec : Specification() {
  @IgnoreIf(IgnoreIfUnknownProperty::class)
  fun foo(a: Int) {
    expect
    false

    where
    variable(a).from(failingDataProvider())
  }
}

class IgnoreIfUnknownDataVariableSpec : Specification() {
  @IgnoreIf(IgnoreIfUnknownDataVariable::class)
  fun foo(a: Int) {
    expect
    false

    where
    variable(a).from(failingDataProvider())
  }
}

class IgnoreIfThrowingConditionSpec : Specification() {
  @IgnoreIf(IgnoreIfThrows::class)
  fun foo(a: Int) {
    expect
    false

    where
    variable(a).from(failingDataProvider())
  }
}

class IgnoreIfUninstantiableConditionSpec : Specification() {
  @IgnoreIf(IgnoreIfUninstantiable::class)
  fun `basic usage`() {
    expect
    false
  }
}

class IgnoreIfAllFalseSpec : Specification() {
  @IgnoreIf(IgnoreIfFalse::class)
  @IgnoreIf(IgnoreIfFalse::class)
  fun foo() {
    expect
    false
  }
}

@IgnoreIf(IgnoreIfAlways::class)
class IgnoreIfSpecTrueSpec : Specification() {
  fun `basic usage`() {
    expect
    false
  }
}

@IgnoreIf(IgnoreIfNever::class)
class IgnoreIfSpecFalseSpec : Specification() {
  fun `basic usage`() {
    expect
    true
  }
}

@IgnoreIf(reason = "dummy message", value = IgnoreIfTrue::class)
class IgnoreIfSpecWithReasonSpec : Specification() {
  fun `basic usage`() {
    expect
    false
  }
}

open class IgnoreIfInheritanceBaseSpec : Specification() {
  fun `base feature`() {
    expect
    true
  }
}

@IgnoreIf(value = IgnoreIfTrue::class, inherited = false)
open class IgnoreIfNotInheritedFooSpec : IgnoreIfInheritanceBaseSpec() {
  fun `foo feature`() {
    expect
    true
  }
}

open class IgnoreIfNotInheritedBarSpec : IgnoreIfNotInheritedFooSpec() {
  fun `bar feature`() {
    expect
    true
  }
}

class IgnoreIfNotInheritedTestSpec : IgnoreIfNotInheritedBarSpec() {
  fun `test feature`() {
    expect
    true
  }
}

@IgnoreIf(value = IgnoreIfTrue::class, inherited = true)
open class IgnoreIfInheritedFooSpec : IgnoreIfInheritanceBaseSpec() {
  fun `foo feature`() {
    expect
    true
  }
}

open class IgnoreIfInheritedBarSpec : IgnoreIfInheritedFooSpec() {
  fun `bar feature`() {
    expect
    true
  }
}

class IgnoreIfInheritedTestSpec : IgnoreIfInheritedBarSpec() {
  fun `test feature`() {
    expect
    true
  }
}

@IgnoreIf(IgnoreIfStaticMethodRunsSpec.Condition::class)
class IgnoreIfStaticMethodRunsSpec : Specification() {
  class Condition(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
    @Suppress("unused")
    fun doCall(context: PreconditionContext<*>): Any? = shouldNotRun()
  }

  fun `basic usage`() {
    expect
    true
  }

  companion object {
    @JvmStatic
    fun shouldNotRun(): Boolean = !true
  }
}

@IgnoreIf(IgnoreIfStaticMethodSkippedSpec.Condition::class)
class IgnoreIfStaticMethodSkippedSpec : Specification() {
  class Condition(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
    @Suppress("unused")
    fun doCall(context: PreconditionContext<*>): Any? = shouldNotRun()
  }

  fun `basic usage`() {
    expect
    true
  }

  companion object {
    @JvmStatic
    fun shouldNotRun(): Boolean = !false
  }
}

@IgnoreIf(IgnoreIfStaticFieldRunsSpec.Condition::class)
class IgnoreIfStaticFieldRunsSpec : Specification() {
  class Condition(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
    @Suppress("unused")
    fun doCall(context: PreconditionContext<*>): Any? = shouldNotRun
  }

  fun `basic usage`() {
    expect
    true
  }

  companion object {
    @JvmField
    val shouldNotRun: Boolean = !true
  }
}

@IgnoreIf(IgnoreIfStaticFieldSkippedSpec.Condition::class)
class IgnoreIfStaticFieldSkippedSpec : Specification() {
  class Condition(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
    @Suppress("unused")
    fun doCall(context: PreconditionContext<*>): Any? = shouldNotRun
  }

  fun `basic usage`() {
    expect
    true
  }

  companion object {
    @JvmField
    val shouldNotRun: Boolean = !false
  }
}

@IgnoreIf(IgnoreIfSharedFieldRunsSpec.Condition::class)
class IgnoreIfSharedFieldRunsSpec : Specification() {
  class Condition(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
    @Suppress("unused")
    fun doCall(context: PreconditionContext<IgnoreIfSharedFieldRunsSpec>): Any? =
      context.shared.shouldRun
  }

  @Shared
  val shouldRun: Boolean = false

  fun `basic usage`() {
    expect
    true
  }
}

@IgnoreIf(IgnoreIfSharedFieldAbortedSpec.Condition::class)
class IgnoreIfSharedFieldAbortedSpec : Specification() {
  class Condition(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
    @Suppress("unused")
    fun doCall(context: PreconditionContext<IgnoreIfSharedFieldAbortedSpec>): Any? =
      context.shared.shouldRun
  }

  @Shared
  val shouldRun: Boolean = true

  fun `basic usage`() {
    expect
    true
  }
}

@IgnoreIf(IgnoreIfInstanceFieldRunsSpec.Condition::class)
class IgnoreIfInstanceFieldRunsSpec : Specification() {
  class Condition(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
    @Suppress("unused")
    fun doCall(context: PreconditionContext<IgnoreIfInstanceFieldRunsSpec>): Any? =
      context.instance.shouldRun
  }

  val shouldRun: Boolean = false

  fun `basic usage`() {
    expect
    true
  }
}

@IgnoreIf(IgnoreIfInstanceFieldAbortedSpec.Condition::class)
class IgnoreIfInstanceFieldAbortedSpec : Specification() {
  class Condition(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
    @Suppress("unused")
    fun doCall(context: PreconditionContext<IgnoreIfInstanceFieldAbortedSpec>): Any? =
      context.instance.shouldRun
  }

  val shouldRun: Boolean = true

  fun `basic usage`() {
    expect
    true
  }
}

class IgnoreIfFeatureSharedFieldSpec : Specification() {
  class Condition(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
    @Suppress("unused")
    fun doCall(context: PreconditionContext<IgnoreIfFeatureSharedFieldSpec>): Any? =
      context.shared.value == 2
  }

  @Shared
  var value = 0

  @IgnoreIf(Condition::class)
  fun `bar`(input: Int) {
    value = input

    expect
    true

    where
    variable(input).from(1, 2, 3)
  }
}

class IgnoreIfFeatureInstanceSharedFieldSpec : Specification() {
  class Condition(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
    @Suppress("unused")
    fun doCall(context: PreconditionContext<IgnoreIfFeatureInstanceSharedFieldSpec>): Any? =
      context.instance.value == 2
  }

  @Shared
  var value = 0

  @IgnoreIf(Condition::class)
  fun `bar`(input: Int) {
    value = input

    expect
    true

    where
    variable(input).from(1, 2, 3)
  }
}

class IgnoreIfFeatureInstanceFieldSpec : Specification() {
  class Condition(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
    @Suppress("unused")
    fun doCall(context: PreconditionContext<IgnoreIfFeatureInstanceFieldSpec>): Any? =
      context.instance.value == 2
  }

  var value = 0

  fun setup() {
    value = staticValue
  }

  @IgnoreIf(Condition::class)
  fun `bar`(input: Int) {
    staticValue = input

    expect
    true

    where
    variable(input).from(1, 2, 3)
  }

  companion object {
    @JvmStatic
    var staticValue = 0
  }
}

class IgnoreIfFeatureStaticFieldSpec : Specification() {
  class IgnoredCondition(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
    @Suppress("unused")
    fun doCall(context: PreconditionContext<*>): Any? = value == 1
  }

  class RunCondition(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
    @Suppress("unused")
    fun doCall(context: PreconditionContext<*>): Any? = value != 1
  }

  @IgnoreIf(IgnoredCondition::class)
  fun bar() {
    expect
    false
  }

  @IgnoreIf(RunCondition::class)
  fun baz() {
    expect
    true
  }

  companion object {
    @JvmStatic
    val value = 1
  }
}
