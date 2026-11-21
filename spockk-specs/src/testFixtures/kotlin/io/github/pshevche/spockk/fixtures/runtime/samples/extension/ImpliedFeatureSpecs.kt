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

import io.github.pshevche.spockk.lang.expect
import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.model.FeatureInfo
import spock.lang.Specification

@Retention(AnnotationRetention.RUNTIME)
@ExtensionAnnotation(ImpliesExtension::class)
annotation class Implies(val value: String)

@Retention(AnnotationRetention.RUNTIME)
@ExtensionAnnotation(ImpliedByExtension::class)
annotation class ImpliedBy(val value: String)

class ImpliesExtension : IAnnotationDrivenExtension<Implies> {
  override fun visitFeatureAnnotation(annotation: Implies, feature: FeatureInfo) {
    feature.addImpliedFeature(feature.parent.allFeatures.first { it.displayName == annotation.value })
  }
}

class ImpliedByExtension : IAnnotationDrivenExtension<ImpliedBy> {
  override fun visitFeatureAnnotation(annotation: ImpliedBy, feature: FeatureInfo) {
    feature.parent.allFeatures.first { it.displayName == annotation.value }.addImpliedFeature(feature)
  }
}

class ImpliedFeatureSameSpec : Specification() {
  fun foo() {
    expect
    true
  }

  @Implies("foo")
  fun bar() {
    expect
    true
  }

  fun baz() {
    expect
    true
  }
}

open class ImpliedFeatureSuper : Specification() {
  fun foo() {
    expect
    true
  }

  fun bam() {
    expect
    true
  }
}

class ImpliesSub : ImpliedFeatureSuper() {
  @Implies("foo")
  fun bar() {
    expect
    true
  }

  fun baz() {
    expect
    true
  }
}

class ImpliedBySub : ImpliedFeatureSuper() {
  @ImpliedBy("foo")
  fun bar() {
    expect
    true
  }

  fun baz() {
    expect
    true
  }
}
