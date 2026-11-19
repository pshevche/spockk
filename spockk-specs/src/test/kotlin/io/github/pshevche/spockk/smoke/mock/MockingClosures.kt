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

import groovy.lang.Closure
import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import spock.lang.Specification

class MockingClosures : Specification() {

  @MigratedFrom("org.spockframework.smoke.mock.MockingClosures#can mock call() method")
  fun `can mock call() method`() {
    given
    val c: Closure<*> = Mock()

    `when`
    c.call()

    then
    1 * c.call()

    `when`
    c.call("one")

    then
    1 * c.call("one")

    `when`
    c.call("one", "two")

    then
    1 * c.call("one", "two")
  }
}
