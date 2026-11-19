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

package io.github.pshevche.spockk.fixtures.runtime.samples.runlistener

import io.github.pshevche.spockk.lang.cleanup
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.runtime.IRunListener
import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.model.ErrorInfo
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.IterationInfo
import org.spockframework.runtime.model.SpecInfo
import spock.lang.Ignore
import spock.lang.Specification

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtensionAnnotation(RegisterRunListenerExtension::class)
annotation class RegisterRunListener

class RegisterRunListenerExtension : IAnnotationDrivenExtension<RegisterRunListener> {
  override fun visitSpecAnnotation(annotation: RegisterRunListener, spec: SpecInfo) {
    spec.addListener(RunListenerDelegate())
  }
}

class RunListenerDelegate : IRunListener {
  companion object {
    @Volatile
    var delegate: IRunListener? = null
  }

  override fun beforeSpec(spec: SpecInfo) {
    delegate?.beforeSpec(spec)
  }

  override fun beforeFeature(feature: FeatureInfo) {
    delegate?.beforeFeature(feature)
  }

  override fun beforeIteration(iteration: IterationInfo) {
    delegate?.beforeIteration(iteration)
  }

  override fun afterIteration(iteration: IterationInfo) {
    delegate?.afterIteration(iteration)
  }

  override fun afterFeature(feature: FeatureInfo) {
    delegate?.afterFeature(feature)
  }

  override fun afterSpec(spec: SpecInfo) {
    delegate?.afterSpec(spec)
  }

  override fun error(error: ErrorInfo) {
    delegate?.error(error)
  }

  override fun specSkipped(spec: SpecInfo) {
    delegate?.specSkipped(spec)
  }

  override fun featureSkipped(feature: FeatureInfo) {
    delegate?.featureSkipped(feature)
  }
}

@RegisterRunListener
@Ignore
class IgnoredListenedSpec : Specification() {
  fun `a test`() {
    expect
    true
  }
}

object ErrorLocation {
  @Volatile
  var failAt: String = ""
}

@RegisterRunListener
class ErrorLocationSpec : Specification() {
  fun setupSpec() {
    check(ErrorLocation.failAt != "setupSpec")
  }

  fun setup() {
    check(ErrorLocation.failAt != "setup")
  }

  fun `a test`() {
    check(ErrorLocation.failAt != "feature start")

    given("setup label")
    check(ErrorLocation.failAt != "feature setup")

    `when`("when label")
    check(ErrorLocation.failAt != "feature when")

    then("then label")
    ErrorLocation.failAt != "feature then"

    cleanup("cleanup label")
    check(ErrorLocation.failAt != "feature cleanup")
  }

  fun `an expect test`() {
    expect("expect label")
    ErrorLocation.failAt != "feature expect"
  }

  fun cleanup() {
    check(ErrorLocation.failAt != "cleanup")
  }

  fun cleanupSpec() {
    check(ErrorLocation.failAt != "cleanupSpec")
  }
}
