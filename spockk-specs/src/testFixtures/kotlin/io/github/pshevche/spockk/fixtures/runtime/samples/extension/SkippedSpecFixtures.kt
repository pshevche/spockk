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
import groovy.lang.MissingPropertyException
import io.github.pshevche.spockk.lang.expect
import org.spockframework.runtime.extension.builtin.PreconditionContext
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.PendingFeatureIf
import spock.lang.Requires
import spock.lang.Specification

class FalseCondition(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Boolean = false
}

class TrueCondition(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Boolean = true
}

class UndeclaredVariableCondition(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Boolean = throw MissingPropertyException("undeclaredVariable")
}

@Requires(FalseCondition::class)
class SkippedRequiresSpecRequiresFeature : Specification() {
  @Requires(UndeclaredVariableCondition::class)
  fun `basic usage`() {
    expect
    true
  }
}

@Requires(FalseCondition::class)
class SkippedRequiresSpecIgnoreIfFeature : Specification() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `basic usage`() {
    expect
    true
  }
}

@Requires(FalseCondition::class)
class SkippedRequiresSpecPendingFeatureIfFeature : Specification() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `basic usage`() {
    expect
    true
  }
}

@IgnoreIf(TrueCondition::class)
class SkippedIgnoreIfSpecRequiresFeature : Specification() {
  @Requires(UndeclaredVariableCondition::class)
  fun `basic usage`() {
    expect
    true
  }
}

@IgnoreIf(TrueCondition::class)
class SkippedIgnoreIfSpecIgnoreIfFeature : Specification() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `basic usage`() {
    expect
    true
  }
}

@IgnoreIf(TrueCondition::class)
class SkippedIgnoreIfSpecPendingFeatureIfFeature : Specification() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `basic usage`() {
    expect
    true
  }
}

@Ignore
class SkippedIgnoreSpecRequiresFeature : Specification() {
  @Requires(UndeclaredVariableCondition::class)
  fun `basic usage`() {
    expect
    true
  }
}

@Ignore
class SkippedIgnoreSpecIgnoreIfFeature : Specification() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `basic usage`() {
    expect
    true
  }
}

@Ignore
class SkippedIgnoreSpecPendingFeatureIfFeature : Specification() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `basic usage`() {
    expect
    true
  }
}

object SkippedSpecFixtures {
  val all: List<Class<*>> = listOf(
    SkippedRequiresSpecRequiresFeature::class.java,
    SkippedRequiresSpecIgnoreIfFeature::class.java,
    SkippedRequiresSpecPendingFeatureIfFeature::class.java,
    SkippedIgnoreIfSpecRequiresFeature::class.java,
    SkippedIgnoreIfSpecIgnoreIfFeature::class.java,
    SkippedIgnoreIfSpecPendingFeatureIfFeature::class.java,
    SkippedIgnoreSpecRequiresFeature::class.java,
    SkippedIgnoreSpecIgnoreIfFeature::class.java,
    SkippedIgnoreSpecPendingFeatureIfFeature::class.java
  )
}
