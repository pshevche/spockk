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
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.runtime.model.IterationFilter
import spock.lang.Specification
import spock.lang.Subject

class IterationFilterSpec : Specification() {

  @Subject
  private val filter = IterationFilter()

  @MigratedFrom("org.spockframework.runtime.model.IterationFilterSpec#allows all indexes by default")
  fun `allows all indexes by default`() {
    expect
    filter.isAllowed(0)
    filter.isAllowed(1)
  }

  @MigratedFrom(
    "org.spockframework.runtime.model.IterationFilterSpec#allows only selected indexes after one has been allowed explicitly"
  )
  fun `allows only selected indexes after one has been allowed explicitly`() {
    `when`
    filter.allow(0)

    then
    filter.isAllowed(0)
    !filter.isAllowed(1)
  }

  @MigratedFrom("org.spockframework.runtime.model.IterationFilterSpec#allows all indexes after configured to do so")
  fun `allows all indexes after configured to do so`() {
    `when`
    filter.allow(0)
    filter.allowAll()

    then
    filter.isAllowed(0)
    filter.isAllowed(1)
  }
}
