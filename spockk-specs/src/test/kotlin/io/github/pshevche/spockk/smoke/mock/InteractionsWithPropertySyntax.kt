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

package io.github.pshevche.spockk.smoke.mock

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.Mock
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import spock.lang.PendingFeature
import spock.lang.Specification

class InteractionsWithPropertySyntax : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.InteractionsWithPropertySyntax#stubbing properties")
  fun `stubbing properties`() {
    val props = Mock(HasProperties::class.java)
    props.length returns 10

    expect
    props.length == 10
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/984")
  @MigratedFrom(
    "org.spockframework.smoke.mock.InteractionsWithPropertySyntax#stubbing properties in initializer closure with 'it'"
  )
  fun `stubbing properties in initializer closure with 'it'`() {
    val props = Mock(HasProperties::class.java) {
      this.length returns 10
    }

    expect
    props.length == 10
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/984")
  @MigratedFrom(
    "org.spockframework.smoke.mock.InteractionsWithPropertySyntax#stubbing properties in initializer closure without 'it'"
  )
  fun `stubbing properties in initializer closure without 'it'`() {
    val props = Mock(HasProperties::class.java) {
      length returns 10
    }

    expect
    props.length == 10
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/984")
  @MigratedFrom("org.spockframework.smoke.mock.InteractionsWithPropertySyntax#mocking properties")
  fun `mocking properties`() {
    val props = Mock(HasProperties::class.java)

    `when`
    props.length
    props.length

    then
    2 * props.length
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.InteractionsWithPropertySyntax#stubbing/mocking boolean properties")
  fun `stubbing mocking boolean properties`() {
    val props = Mock(HasProperties::class.java)
    props.isEmpty returns true
    props.full returns false

    expect
    props.isEmpty
    !props.full
  }

  interface HasProperties {
    var length: Int
    val isEmpty: Boolean
    val full: Boolean
  }
}
