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

package io.github.pshevche.spockk.util.matcher

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.hamcrest.CoreMatchers.not
import org.spockframework.runtime.ConditionNotSatisfiedError
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.util.matcher.HamcrestMatchers.closeTo
import spock.util.matcher.HamcrestSupport.that

class IsCloseToSpec : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/875")
  @MigratedFrom("spock.util.matcher.IsCloseToSpec#compare Integers that are close enough")
  fun `compare Integers that are close enough`() {
    expect
    that(3, closeTo(2, 1))
    that(3, closeTo(3, 0))
    that(3, closeTo(4, 1))
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/875")
  @MigratedFrom("spock.util.matcher.IsCloseToSpec#compare Integers that are not close enough")
  fun `compare Integers that are not close enough`() {
    `when`
    that(3, closeTo(1, 1))

    then
    thrown(ConditionNotSatisfiedError::class.java)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/875")
  @MigratedFrom("spock.util.matcher.IsCloseToSpec#compare BigDecimals that are close enough")
  fun `compare BigDecimals that are close enough`() {
    expect
    that(3.1415.toBigDecimal(), closeTo(3.1.toBigDecimal(), 0.05.toBigDecimal()))
    that(3.1415.toBigDecimal(), closeTo(3.1415000.toBigDecimal(), 0))
    that(3.1415.toBigDecimal(), closeTo(3.2.toBigDecimal(), 0.06.toBigDecimal()))
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/875")
  @MigratedFrom("spock.util.matcher.IsCloseToSpec#compare BigDecimals that are not close enough")
  fun `compare BigDecimals that are not close enough`() {
    `when`
    that(3.1415.toBigDecimal(), closeTo(3.0.toBigDecimal(), 0.05.toBigDecimal()))

    then
    thrown(ConditionNotSatisfiedError::class.java)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/875")
  @MigratedFrom("spock.util.matcher.IsCloseToSpec#mix integer and floating point numbers")
  fun `mix integer and floating point numbers`() {
    expect
    that(3.1415.toBigDecimal(), closeTo(3, 0.15.toBigDecimal()))
    that(3, closeTo(3.2.toBigDecimal(), 1))
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/875")
  @MigratedFrom("spock.util.matcher.IsCloseToSpec#compare infinity")
  fun `compare infinity`() {
    expect
    that(Float.POSITIVE_INFINITY, closeTo(Float.POSITIVE_INFINITY, 0.1))
    that(Double.NEGATIVE_INFINITY, closeTo(Double.NEGATIVE_INFINITY, 0.0))
    that(Float.POSITIVE_INFINITY, closeTo(Double.POSITIVE_INFINITY, 0.0))
    that(Float.POSITIVE_INFINITY, not(closeTo(Float.NEGATIVE_INFINITY, 99999)))
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/875")
  @MigratedFrom("spock.util.matcher.IsCloseToSpec#compare NaN")
  fun `compare NaN`() {
    expect
    that(Float.NaN, closeTo(Float.NaN, 0.1))
    that(Double.NaN, closeTo(Double.NaN, 0.0))
    that(Float.NaN, closeTo(Double.NaN, 0.0))
    that(Float.NaN, not(closeTo(9999.9f, 999999999)))
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/875")
  @MigratedFrom("spock.util.matcher.IsCloseToSpec#error message uses correct values")
  fun `error message uses correct values`() {
    `when`
    that(2.2, closeTo(3.3, 0.5))

    then
    val e = thrown(ConditionNotSatisfiedError::class.java)
    e.toString().contains("Expected: a numeric value within <0.5> of <3.3>\n     but: <2.2> differed by <1.1>")
  }
}
