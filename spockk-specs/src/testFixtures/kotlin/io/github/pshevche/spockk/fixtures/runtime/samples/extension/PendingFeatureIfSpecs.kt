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
import org.spockframework.runtime.extension.builtin.PreconditionContext
import spock.lang.PendingFeature
import spock.lang.PendingFeatureIf
import spock.lang.Shared
import spock.lang.Specification

class IsTrue(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  fun doCall(ctx: PreconditionContext<*>): Boolean = true
}

class IsFalse(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  fun doCall(ctx: PreconditionContext<*>): Boolean = false
}

class DataAIs1(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  fun doCall(ctx: PreconditionContext<*>): Boolean = ctx.data["a"] == 1
}

class DataAIs2(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  fun doCall(ctx: PreconditionContext<*>): Boolean = ctx.data["a"] == 2
}

class SharedValueIs2(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  fun doCall(ctx: PreconditionContext<*>): Boolean = ctx.instance.let { (it as SharedFieldAccessSpec).value } == 2
}

class InstanceValueIs2(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  fun doCall(ctx: PreconditionContext<*>): Boolean = ctx.instance.let { (it as InstanceFieldAccessSpec).value } == 2
}

class StaticValueIs1(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  fun doCall(ctx: PreconditionContext<*>): Boolean = StaticFieldAccessSpec.value == 1
}

class StaticValueIsNot1(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  fun doCall(ctx: PreconditionContext<*>): Boolean = StaticFieldAccessSpec.value != 1
}

class FailingConditionTrue : Specification() {
  @PendingFeatureIf(IsTrue::class)
  fun bar() {
    expect
    assert(false)
  }
}

class FailingConditionTrueTwice : Specification() {
  @PendingFeatureIf(IsTrue::class)
  @PendingFeatureIf(IsTrue::class)
  fun bar() {
    expect
    assert(false)
  }
}

class PassingConditionTrue : Specification() {
  @PendingFeatureIf(IsTrue::class)
  fun bar() {
    expect
    assert(true)
  }
}

class PassingConditionTrueTwice : Specification() {
  @PendingFeatureIf(IsTrue::class)
  @PendingFeatureIf(IsTrue::class)
  fun bar() {
    expect
    assert(true)
  }
}

class PassingPendingFeatureFirst : Specification() {
  @PendingFeature
  @PendingFeatureIf(IsTrue::class)
  fun bar() {
    expect
    assert(true)
  }
}

class PassingParameterizedPendingFeatureFirst : Specification() {
  @PendingFeature
  @PendingFeatureIf(IsTrue::class)
  fun bar(a: Int) {
    expect
    assert(true)

    where
    variable(a).from(1)
  }
}

class PassingDataConditionPendingFeatureFirst : Specification() {
  @PendingFeature
  @PendingFeatureIf(DataAIs1::class)
  fun bar(a: Int) {
    expect
    assert(true)

    where
    variable(a).from(1)
  }
}

class PassingPendingFeatureAfter : Specification() {
  @PendingFeatureIf(IsTrue::class)
  @PendingFeature
  fun bar() {
    expect
    assert(true)
  }
}

class PassingParameterizedPendingFeatureAfter : Specification() {
  @PendingFeatureIf(IsTrue::class)
  @PendingFeature
  fun bar(a: Int) {
    expect
    assert(true)

    where
    variable(a).from(1)
  }
}

class PassingDataConditionPendingFeatureAfter : Specification() {
  @PendingFeatureIf(DataAIs1::class)
  @PendingFeature
  fun bar(a: Int) {
    expect
    assert(true)

    where
    variable(a).from(1)
  }
}

class FailingConditionFalse : Specification() {
  @PendingFeatureIf(IsFalse::class)
  fun bar() {
    expect
    assert(false)
  }
}

class PassingConditionFalse : Specification() {
  @PendingFeatureIf(IsFalse::class)
  fun bar() {
    expect
    assert(true)
  }
}

class FailingDataConditionTrue : Specification() {
  @PendingFeatureIf(DataAIs1::class)
  fun bar(a: Int) {
    expect
    assert(a == 2)

    where
    variable(a).from(1)
  }
}

class FailingDataConditionTrueTwice : Specification() {
  @PendingFeatureIf(DataAIs1::class)
  @PendingFeatureIf(DataAIs1::class)
  fun bar(a: Int) {
    expect
    assert(a == 2)

    where
    variable(a).from(1)
  }
}

class PassingDataConditionTrue : Specification() {
  @PendingFeatureIf(DataAIs2::class)
  fun bar(a: Int) {
    expect
    assert(a == 2)

    where
    variable(a).from(2)
  }
}

class PassingDataConditionTrueTwice : Specification() {
  @PendingFeatureIf(DataAIs2::class)
  @PendingFeatureIf(DataAIs2::class)
  fun bar(a: Int) {
    expect
    assert(a == 2)

    where
    variable(a).from(2)
  }
}

class FailingDataConditionFalse : Specification() {
  @PendingFeatureIf(DataAIs2::class)
  fun bar(a: Int) {
    expect
    assert(a == 2)

    where
    variable(a).from(1)
  }
}

class PassingDataConditionFalse : Specification() {
  @PendingFeatureIf(DataAIs1::class)
  fun bar(a: Int) {
    expect
    assert(a == 2)

    where
    variable(a).from(2)
  }
}

class SharedFieldAccessSpec : Specification() {
  @Shared var value = 0

  @PendingFeatureIf(SharedValueIs2::class)
  fun `bar #input`(input: Int) {
    value = input

    expect
    assert(input != 3)

    where
    variable(input).from(1, 2, 3)
  }
}

class InstanceFieldAccessSpec : Specification() {
  var value = 0

  fun setup() {
    value = staticValue
  }

  @PendingFeatureIf(InstanceValueIs2::class)
  fun `bar #input`(input: Int) {
    staticValue = input

    expect
    assert(input != 3)

    where
    variable(input).from(1, 2, 3)
  }

  companion object {
    @JvmStatic var staticValue = 0
  }
}

class StaticFieldAccessSpec : Specification() {
  @PendingFeatureIf(StaticValueIs1::class)
  fun bar() {
    expect
    assert(false)
  }

  @PendingFeatureIf(StaticValueIsNot1::class)
  fun baz() {
    expect
    assert(true)
  }

  companion object {
    @JvmStatic var value = 1
  }
}
