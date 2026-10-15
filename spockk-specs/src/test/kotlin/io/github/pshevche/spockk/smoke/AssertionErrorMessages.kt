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

package io.github.pshevche.spockk.smoke

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.verify
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.runtime.SpockAssertionError
import spock.lang.Issue
import spock.lang.Specification

@Issue("https://github.com/spockframework/spock/issues/217")
class AssertionErrorMessages : Specification() {

  @MigratedFrom(
    "org.spockframework.smoke.AssertionErrorMessages#assertion error returns (same) message for getMessage() and toString()"
  )
  fun `assertion error returns (same) message for getMessage() and toString()`() {
    `when`
    failingCondition()

    then
    val e = thrown(SpockAssertionError::class.java)
    !e.message.isNullOrEmpty()
    e.message!!.contains("this == 2")
    e.message == e.toString()
  }

  private fun failingCondition() {
    verify(1) {
      this == 2
    }
  }
}
