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
import io.github.pshevche.spockk.lang.expect
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.lang.Tag

@Tag("class-level")
open class TagExtension : Specification() {

  @MigratedFrom(
    "org.spockframework.smoke.extension.TagExtension#class level tags",
    "org.spockframework.smoke.extension.InheritedTagExtension#class level tags"
  )
  fun `class level tags`() {
    expect
    tags() == setOf("class-level")
  }

  @Tag("method-level")
  @MigratedFrom(
    "org.spockframework.smoke.extension.TagExtension#method level tags",
    "org.spockframework.smoke.extension.InheritedTagExtension#method level tags"
  )
  fun `method level tags`() {
    expect
    tags() == setOf("class-level", "method-level")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/894")
  @Tag("multi-tags")
  @Tag("method-level")
  @Tag("method-level")
  @MigratedFrom(
    "org.spockframework.smoke.extension.TagExtension#multi tags",
    "org.spockframework.smoke.extension.InheritedTagExtension#multi tags"
  )
  fun `multi tags`() {
    expect
    tags() == setOf("class-level", "method-level", "multi-tags")
  }

  protected fun tags(): Set<String> = specificationContext.currentFeature.testTags.map { it.value }.toSet()
}

class InheritedTagExtension : TagExtension() {

  @MigratedFrom("org.spockframework.smoke.extension.InheritedTagExtension#inherited tags")
  fun `inherited tags`() {
    expect
    tags() == setOf("class-level")
  }
}
