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

package io.github.pshevche.spockk.fixtures.runtime.samples.parameterization

import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.where
import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.model.IterationInfo
import org.spockframework.runtime.model.NameProvider
import org.spockframework.runtime.model.SpecInfo
import spock.lang.Rollup
import spock.lang.Specification
import spock.lang.Unroll

class Actor {
  val details: Map<String, Any> = mapOf("name" to "fred", "age" to 30)
}

class FailingObject {
  fun ok() = "ok"

  fun bang(): String = throw NullPointerException()

  override fun toString(): String = throw RuntimeException()
}

class UnrolledSimpleSpec : Specification() {
  fun foo(x: Int) {
    expect
    true

    where
    variable(x).from(1, 2, 3)
  }
}

class UnrolledFailingProviderSpec : Specification() {
  fun `a feature`(x: Int, y: Int) {
    expect
    true

    where
    variable(x).from(1)
    variable(y).from(failingProvider())
  }
}

private fun failingProvider(): List<Int> = throw Exception()

class UnrolledPatternSpec : Specification() {
  @Unroll("one #y two #x three")
  fun foo(x: Int, y: String) {
    expect
    true

    where
    variable(x).from(1, 2, 3)
    variable(y).from("a", "b", "c")
  }
}

class UnrolledFeatureNameAndIndexPatternSpec : Specification() {
  @Unroll("one #featureName two #iterationIndex three")
  fun foo(x: Int, y: String) {
    expect
    true

    where
    variable(x).from(1, 2, 3)
    variable(y).from("a", "b", "c")
  }
}

class UnrolledDataVariablesPatternSpec : Specification() {
  @Unroll("one #dataVariables two")
  fun foo(x: Int, y: String) {
    expect
    true

    where
    variable(x).from(1, 2, 3)
    variable(y).from("a", "b", "c")
  }
}

class UnrolledDataVariablesWithIndexPatternSpec : Specification() {
  @Unroll("one #dataVariablesWithIndex two")
  fun foo(x: Int, y: String) {
    expect
    true

    where
    variable(x).from(1, 2, 3)
    variable(y).from("a", "b", "c")
  }
}

class UnrolledDefaultNamingSpec : Specification() {
  fun foo(x: Int, y: String) {
    expect
    true

    where
    variable(x).from(1, 2, 3)
    variable(y).from("a", "b", "c")
  }
}

class UnrolledDataVariablesWithIndexOnlyPatternSpec : Specification() {
  @Unroll("#dataVariablesWithIndex")
  fun foo(x: Int, y: String) {
    expect
    true

    where
    variable(x).from(1, 2, 3)
    variable(y).from("a", "b", "c")
  }
}

class UnrolledNullVariablePatternSpec : Specification() {
  @Unroll("one #x two #y three")
  fun foo(x: Int, y: String?) {
    expect
    true

    where
    variable(x).from(1)
    variable(y).from(listOf<String?>(null))
  }
}

class UnrolledPropertyExpressionPatternSpec : Specification() {
  @Unroll("one #actor.details.name two")
  fun foo(actor: Actor) {
    expect
    true

    where
    variable(actor).from(Actor())
  }
}

class UnrolledZeroArgMethodPatternSpec : Specification() {
  @Unroll("one #actor.details.name.size() two")
  fun foo(actor: Actor) {
    expect
    true

    where
    variable(actor).from(Actor())
  }
}

class UnrolledUnevaluablePatternSpec : Specification() {
  @Unroll("#obj #obj.ok() #obj.bang() #obj.missing() #missing")
  fun foo(obj: FailingObject) {
    expect
    true

    where
    variable(obj).from(FailingObject())
  }
}

@Unroll("a feature")
class ClassLevelUnrollSpec : Specification() {
  @Unroll("#actor.details.name")
  fun method(actor: Actor) {
    expect
    true

    where
    variable(actor).from(Actor())
  }
}

@Rollup
class ClassLevelRollupSpec : Specification() {
  @Unroll("#actor.details.name")
  fun method(actor: Actor) {
    expect
    true

    where
    variable(actor).from(Actor())
  }
}

class MethodLevelUnrollSpec : Specification() {
  @Unroll("#actor.details.name")
  fun method(actor: Actor) {
    expect
    true

    where
    variable(actor).from(Actor())
  }
}

@Unroll("#actor.details.name")
class ClassLevelUnrollOnlySpec : Specification() {
  fun method(actor: Actor) {
    expect
    true

    where
    variable(actor).from(Actor())
  }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@ExtensionAnnotation(SuffixedExtension::class)
annotation class Suffixed

class SuffixedExtension : IAnnotationDrivenExtension<Suffixed> {
  override fun visitSpecAnnotation(annotation: Suffixed, spec: SpecInfo) {
    spec.features.forEach { feature ->
      feature.isReportIterations = true
      val current = feature.iterationNameProvider
      feature.iterationNameProvider = NameProvider<IterationInfo> {
        (if (current == null) feature.name else current.getName(it)) + " [suffix]"
      }
      feature.displayName = feature.name + " [suffix]"
    }
  }
}

@Suffixed
class CustomNameProviderSpec : Specification() {
  fun test() {
    expect
    true
  }

  fun parameterized(t: Int) {
    expect
    true

    where
    variable(t).from(0, 1)
  }
}
