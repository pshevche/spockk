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
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IssueOnFixtureMethodSpec
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.verify
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.EngineTestKit
import org.spockframework.runtime.InvalidSpecException
import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.Specification

@Issue("https://my.issues.org/FOO-123")
class IssueExtension : Specification() {

  @MigratedFrom("org.spockframework.smoke.extension.IssueExtension#spec-level @Issue annotation is converted to tag")
  fun `spec-level @Issue annotation is converted to tag`() {
    val tags = specificationContext.currentSpec.tags

    expect
    tags.size == 1
    verify(tags[0]) {
      name == "FOO-123"
      key == "issue"
      value == "FOO-123"
      url == "https://my.issues.org/FOO-123"
    }
  }

  @Issue("https://my.issues.org/FOO-456")
  @MigratedFrom("org.spockframework.smoke.extension.IssueExtension#feature-level @Issue annotation is converted to tag")
  fun `feature-level @Issue annotation is converted to tag`() {
    val tags = specificationContext.currentFeature.tags

    expect
    tags.size == 1
    verify(tags[0]) {
      name == "FOO-456"
      key == "issue"
      value == "FOO-456"
      url == "https://my.issues.org/FOO-456"
    }
  }

  @Issue("https://my.issues.org/FOO-1", "https://my.issues.org/FOO-2")
  @MigratedFrom("org.spockframework.smoke.extension.IssueExtension#if multiple issues are listed, all of them are converted to a tag")
  fun `if multiple issues are listed, all of them are converted to a tag`() {
    val tags = specificationContext.currentFeature.tags

    expect
    tags.size == 2
    verify(tags[0]) {
      name == "FOO-1"
      key == "issue"
      value == "FOO-1"
      url == "https://my.issues.org/FOO-1"
    }
    verify(tags[1]) {
      name == "FOO-2"
      key == "issue"
      value == "FOO-2"
      url == "https://my.issues.org/FOO-2"
    }
  }

  @Issue("https://my.issues.org/FOO-1")
  @Issue("https://my.issues.org/FOO-2")
  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/894")
  @MigratedFrom(
    "org.spockframework.smoke.extension.IssueExtension#if multiple issue annotations are applied, all of them are converted to a tag"
  )
  fun `if multiple issue annotations are applied, all of them are converted to a tag`() {
    val tags = specificationContext.currentFeature.tags

    expect
    tags.size == 2
    verify(tags[0]) {
      name == "FOO-1"
      key == "issue"
      value == "FOO-1"
      url == "https://my.issues.org/FOO-1"
    }
    verify(tags[1]) {
      name == "FOO-2"
      key == "issue"
      value == "FOO-2"
      url == "https://my.issues.org/FOO-2"
    }
  }

  @Issue("https://my.issues.org/FOO-1", "https://my.issues.org/FOO-2")
  @Issue("https://my.issues.org/FOO-3", "https://my.issues.org/FOO-4")
  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/894")
  @MigratedFrom(
    "org.spockframework.smoke.extension.IssueExtension#if multiple issue annotations with multiple issues are applied, all of them are converted to a tag"
  )
  fun `if multiple issue annotations with multiple issues are applied, all of them are converted to a tag`() {
    val tags = specificationContext.currentFeature.tags

    expect
    tags.size == 4
    verify(tags[0]) {
      name == "FOO-1"
      key == "issue"
      value == "FOO-1"
      url == "https://my.issues.org/FOO-1"
    }
    verify(tags[1]) {
      name == "FOO-2"
      key == "issue"
      value == "FOO-2"
      url == "https://my.issues.org/FOO-2"
    }
    verify(tags[2]) {
      name == "FOO-3"
      key == "issue"
      value == "FOO-3"
      url == "https://my.issues.org/FOO-3"
    }
    verify(tags[3]) {
      name == "FOO-4"
      key == "issue"
      value == "FOO-4"
      url == "https://my.issues.org/FOO-4"
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.IssueExtension#complains if @Issue is used on anything other than a spec or feature")
  fun `complains if @Issue is used on anything other than a spec or feature`() {
    `when`
    // The invalid annotation fails the spec while it is being built, so it surfaces as a container failure.
    val results = EngineTestKit.engine("spock")
      .selectors(selectClass(IssueOnFixtureMethodSpec::class.java))
      .execute()

    then
    val throwable = results.containerEvents().failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
    throwable is InvalidSpecException
    throwable.message!!.contains("may not be applied to fixture methods")
  }
}
