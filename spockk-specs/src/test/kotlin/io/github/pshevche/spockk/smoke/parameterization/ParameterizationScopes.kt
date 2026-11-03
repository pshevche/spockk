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

package io.github.pshevche.spockk.smoke.parameterization

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import spock.lang.Specification

class ParameterizationScopes : Specification() {

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizationScopes#in outer scope")
  fun `in outer scope`(x: Int) {
    expect
    x == 1

    where
    variable(x).from(1)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizationScopes#in inner scope")
  fun `in inner scope`(inc: Int) {
    var sum = 0

    `when`
    for (i in 1..3) {
      sum += inc
    }

    then
    sum == 6

    where
    variable(inc).from(2)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizationScopes#in closure scope")
  fun `in closure scope`(inc: Int) {
    var sum = 0

    `when`
    (1..3).forEach {
      sum += inc
    }

    then
    sum == 6

    where
    variable(inc).from(2)
  }
}
