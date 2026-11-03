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

import groovy.lang.Closure
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.where
import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.extension.builtin.PreconditionContext
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.MethodInfo
import org.spockframework.runtime.model.MethodInfo.MISSING_ARGUMENT
import spock.lang.Requires
import spock.lang.Specification

@Retention(AnnotationRetention.RUNTIME)
@ExtensionAnnotation(FooExtension::class)
annotation class Foo

@Retention(AnnotationRetention.RUNTIME)
@ExtensionAnnotation(BarExtension::class)
annotation class Bar

/** Fills the missing argument by mutating the arguments array in place. */
class FooExtension : IAnnotationDrivenExtension<Foo> {
  override fun visitFixtureAnnotation(annotation: Foo, fixtureMethod: MethodInfo) {
    fixtureMethod.addInterceptor {
      check(it.arguments.size == 1)
      check(it.arguments.first() === MISSING_ARGUMENT)
      it.arguments[0] = "foo"
      it.proceed()
    }
  }

  override fun visitFeatureAnnotation(annotation: Foo, feature: FeatureInfo) {
    feature.featureMethod.addInterceptor {
      check(it.arguments.size == 3)
      check(it.arguments[2] === MISSING_ARGUMENT)
      it.arguments[2] = "foo"
      it.proceed()
    }
  }
}

/** Fills the missing argument by replacing the whole arguments array. */
class BarExtension : IAnnotationDrivenExtension<Bar> {
  override fun visitFixtureAnnotation(annotation: Bar, fixtureMethod: MethodInfo) {
    fixtureMethod.addInterceptor {
      check(it.arguments.size == 1)
      check(it.arguments.first() === MISSING_ARGUMENT)
      it.arguments = arrayOf("bar")
      it.proceed()
    }
  }

  override fun visitFeatureAnnotation(annotation: Bar, feature: FeatureInfo) {
    feature.featureMethod.addInterceptor {
      check(it.arguments.size == 3)
      check(it.arguments[2] === MISSING_ARGUMENT)
      it.arguments = it.arguments.clone().also { args -> args[2] = "bar" }
      it.proceed()
    }
  }
}

class DataValueX(owner: Any?, thisObject: Any?) : Closure<Any?>(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Any? = context.data["x"]
}

class MoreParametersThanDataVariablesSpec : Specification() {
  fun foo(x: Int, y: Int, z: Any?) {
    expect
    x == y
    z == null

    where
    variable(x).from(1, 2)
    variable(y).from(1, 2)
  }
}

class UnprovidedFeatureParametersSpec : Specification() {
  fun foo(x: Any?, y: Any?) {
    expect
    x == y
  }
}

class AdditionalArgumentsDisplayNameSpec : Specification() {
  @Foo
  fun `a feature`(x: Int, y: Int, a: Any?) {
    expect
    true

    where
    variable(x).from(1, 2)
    variable(y).from(3, 4)
  }
}

class AdditionalArgumentsRequiresSpec : Specification() {
  @Foo
  @Requires(DataValueX::class)
  fun `a feature`(x: Int, y: Int, a: Any?) {
    expect
    true

    where
    variable(x).from(1)
    variable(y).from(2)
  }
}
