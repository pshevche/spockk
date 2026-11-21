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

import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.extension.IStatelessAnnotationDrivenExtension
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.MethodInfo
import org.spockframework.runtime.model.SpecInfo
import spock.config.ConfigurationObject
import java.util.concurrent.atomic.AtomicInteger

@ConfigurationObject("extensionCounter")
class ExtensionCounter {
  var statefulCounter: AtomicInteger? = null
  var statelessCounter: AtomicInteger? = null
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@ExtensionAnnotation(StatefulExtension::class)
annotation class Stateful

class StatefulExtension(extensionCounter: ExtensionCounter) : IAnnotationDrivenExtension<Stateful> {
  // Property initializer instead of init {}: https://github.com/pshevche/spockk/issues/739
  @Suppress("unused")
  private val instanceCount = extensionCounter.statefulCounter!!.incrementAndGet()

  override fun visitSpecAnnotation(annotation: Stateful, spec: SpecInfo) = Unit

  override fun visitFieldAnnotation(annotation: Stateful, field: FieldInfo) = Unit

  override fun visitFeatureAnnotation(annotation: Stateful, feature: FeatureInfo) = Unit

  override fun visitFixtureAnnotation(annotation: Stateful, fixtureMethod: MethodInfo) = Unit
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@ExtensionAnnotation(StatelessExtension::class)
annotation class Stateless

class StatelessExtension(extensionCounter: ExtensionCounter) : IStatelessAnnotationDrivenExtension<Stateless> {
  // Property initializer instead of init {}: https://github.com/pshevche/spockk/issues/739
  @Suppress("unused")
  private val instanceCount = extensionCounter.statelessCounter!!.incrementAndGet()

  override fun visitSpecAnnotation(annotation: Stateless, spec: SpecInfo) = Unit

  override fun visitFieldAnnotation(annotation: Stateless, field: FieldInfo) = Unit

  override fun visitFeatureAnnotation(annotation: Stateless, feature: FeatureInfo) = Unit

  override fun visitFixtureAnnotation(annotation: Stateless, fixtureMethod: MethodInfo) = Unit
}
