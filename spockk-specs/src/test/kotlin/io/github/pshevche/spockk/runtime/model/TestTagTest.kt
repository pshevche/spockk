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

package io.github.pshevche.spockk.runtime.model

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.spockframework.runtime.model.TestTag
import spock.lang.Specification

class TestTagTest : Specification() {

  @MigratedFrom("org.spockframework.runtime.model.TestTagTest#allows valid tags")
  fun `allows valid tags`(tag: String) {
    expect
    TestTag.create(tag) != null

    where
    variable(tag).from("tag", "tag_with_underscore", "tag-with-dash", "ʘ‿ʘ", "♥╣[-_-]╠♥️", "💩", "\"'..?")
  }

  @MigratedFrom("org.spockframework.runtime.model.TestTagTest#forbids invalid characters in tags")
  fun `forbids invalid characters in tags`(tag: String, readableName: String) {
    `when`
    TestTag.create(tag)

    then
    val e = thrown(IllegalArgumentException::class.java)
    e.message!!.contains(readableName)

    where
    variable(tag).from(" ", "\n", "\t", "\r", "\u0000", "(", ")", "&", "|", "!")
    variable(readableName).from(Character.getName(tag.codePointAt(0)))
  }

  @MigratedFrom("org.spockframework.runtime.model.TestTagTest#forbids empty or null tags")
  fun `forbids empty or null tags`(tag: String?) {
    `when`
    TestTag.create(tag)

    then
    thrown(IllegalArgumentException::class.java)

    where
    variable(tag).from("", null)
  }
}
