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

package io.github.pshevche.spockk.smoke.condition

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.hamcrest.CoreMatchers.equalTo
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.util.matcher.HamcrestSupport
import spock.util.matcher.HamcrestSupport.that

class MatcherConditions : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/875")
  @MigratedFrom("org.spockframework.smoke.condition.MatcherConditions#have an alternative 'that' syntax")
  fun `have an alternative 'that' syntax`() {
    val x = 42

    expect
    that(x, equalTo(42))
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/875")
  @MigratedFrom("org.spockframework.smoke.condition.MatcherConditions#have an alternative 'expect' syntax")
  fun `have an alternative 'expect' syntax`() {
    `when`
    val x = 42

    then
    HamcrestSupport.expect(x, equalTo(42))
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/875")
  @MigratedFrom("org.spockframework.smoke.condition.MatcherConditions#fail when matcher doesn't match (3)")
  fun `fail when matcher doesn't match (3)`() {
    val x = 21

    expect
    that(x, equalTo(42))
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/875")
  @MigratedFrom(
    "org.spockframework.smoke.condition.MatcherConditions#can have a string literal as actual if 'that' or 'expect' syntax is used"
  )
  fun `can have a string literal as actual if 'that' or 'expect' syntax is used`() {
    val x = "fred"

    expect
    that("fred", equalTo(x))
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/875")
  @MigratedFrom(
    "org.spockframework.smoke.condition.MatcherConditions#can have a method expression as actual if 'that' or 'expect' syntax is used"
  )
  fun `can have a method expression as actual if 'that' or 'expect' syntax is used`() {
    val foo = Foo()

    expect
    that(foo.method(), equalTo("method"))
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/875")
  @MigratedFrom(
    "org.spockframework.smoke.condition.MatcherConditions#can have a property expression as actual if 'that' or 'expect' syntax is used"
  )
  fun `can have a property expression as actual if 'that' or 'expect' syntax is used`() {
    val foo = Foo()

    expect
    that(foo.property, equalTo("property"))
  }

  class Foo {
    fun method(): String = "method"

    val property: String = "property"
  }
}
