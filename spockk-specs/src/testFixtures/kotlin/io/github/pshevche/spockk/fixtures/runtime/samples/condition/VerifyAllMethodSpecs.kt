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

package io.github.pshevche.spockk.fixtures.runtime.samples.condition

import io.github.pshevche.spockk.lang.expect
import spock.lang.Specification
import spock.lang.VerifyAll

class VerifyAllMethodSpec : Specification() {

  @VerifyAll
  private fun isPositiveAndEven(x: Int) {
    x > 0
    x % 2 == 0
  }

  fun `feature`() {
    expect
    isPositiveAndEven(-3)
  }
}

object VerifyAllAssertions {
  @VerifyAll
  fun isPositiveAndEven(x: Int) {
    x > 0
    x % 2 == 0
  }
}

class VerifyAllHelperMethodSpec : Specification() {
  fun `feature`() {
    expect
    VerifyAllAssertions.isPositiveAndEven(-3)
  }
}
