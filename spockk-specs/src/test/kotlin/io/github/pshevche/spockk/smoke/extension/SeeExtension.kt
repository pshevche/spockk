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

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.SeeOnFixtureMethodSpec
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.EngineTestKit
import org.spockframework.runtime.InvalidSpecException
import spock.lang.PendingFeature
import spock.lang.See
import spock.lang.Specification

@See("https://spockframework.org/spec")
class SeeExtension : Specification() {

  @MigratedFrom("org.spockframework.smoke.extension.SeeExtension#adds attachment for spec-level @See annotation")
  fun `adds attachment for spec-level @See annotation`() {
    val attachments = specificationContext.currentSpec.attachments

    expect
    attachments.size == 1
    attachments[0].name == "https://spockframework.org/spec"
    attachments[0].url == "https://spockframework.org/spec"
  }

  @See("https://spockframework.org/feature")
  @MigratedFrom("org.spockframework.smoke.extension.SeeExtension#adds attachment for feature-level @See annotation")
  fun `adds attachment for feature-level @See annotation`() {
    val attachments = specificationContext.currentFeature.attachments

    expect
    attachments.size == 1
    attachments[0].name == "https://spockframework.org/feature"
    attachments[0].url == "https://spockframework.org/feature"
  }

  @See("https://spockframework.org/one", "https://spockframework.org/two")
  @MigratedFrom("org.spockframework.smoke.extension.SeeExtension#adds attachment for each URL")
  fun `adds attachment for each URL`() {
    val attachments = specificationContext.currentFeature.attachments

    expect
    attachments.size == 2
    attachments[0].name == "https://spockframework.org/one"
    attachments[0].url == "https://spockframework.org/one"
    attachments[1].name == "https://spockframework.org/two"
    attachments[1].url == "https://spockframework.org/two"
  }

  @See("https://spockframework.org/one")
  @See("https://spockframework.org/two")
  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/894")
  @MigratedFrom("org.spockframework.smoke.extension.SeeExtension#adds attachment for each URL, if multiple see annotations are applied")
  fun `adds attachment for each URL, if multiple see annotations are applied`() {
    val attachments = specificationContext.currentFeature.attachments

    expect
    attachments.size == 2
    attachments[0].name == "https://spockframework.org/one"
    attachments[0].url == "https://spockframework.org/one"
    attachments[1].name == "https://spockframework.org/two"
    attachments[1].url == "https://spockframework.org/two"
  }

  @See("https://spockframework.org/one", "https://spockframework.org/two")
  @See("https://spockframework.org/three", "https://spockframework.org/four")
  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/894")
  @MigratedFrom("org.spockframework.smoke.extension.SeeExtension#adds attachment for each URL, if multiple see annotations with multiple values are applied")
  fun `adds attachment for each URL, if multiple see annotations with multiple values are applied`() {
    val attachments = specificationContext.currentFeature.attachments

    expect
    attachments.size == 4
    attachments[0].name == "https://spockframework.org/one"
    attachments[0].url == "https://spockframework.org/one"
    attachments[1].name == "https://spockframework.org/two"
    attachments[1].url == "https://spockframework.org/two"
    attachments[2].name == "https://spockframework.org/three"
    attachments[2].url == "https://spockframework.org/three"
    attachments[3].name == "https://spockframework.org/four"
    attachments[3].url == "https://spockframework.org/four"
  }

  @MigratedFrom("org.spockframework.smoke.extension.SeeExtension#barks if @See is used on anything other than a spec or feature")
  fun `barks if @See is used on anything other than a spec or feature`() {
    `when`
    // The invalid annotation fails the spec while it is being built, so it surfaces as a container failure.
    val results = EngineTestKit.engine("spock")
      .selectors(selectClass(SeeOnFixtureMethodSpec::class.java))
      .execute()

    then
    val throwable = results.containerEvents().failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
    throwable is InvalidSpecException
    throwable.message!!.contains("may not be applied to fixture methods")
  }
}
