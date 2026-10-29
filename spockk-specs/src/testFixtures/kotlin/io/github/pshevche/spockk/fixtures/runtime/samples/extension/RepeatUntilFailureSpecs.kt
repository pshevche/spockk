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

package io.github.pshevche.spockk.fixtures.runtime.samples.extension

import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import org.opentest4j.TestAbortedException
import spock.lang.RepeatUntilFailure
import spock.lang.Shared
import spock.lang.Specification

class RepeatUntilFailureFirstFailureSpec : Specification() {
  @Shared var count = 0

  @RepeatUntilFailure
  fun test() {
    count++
    expect
    count < 3
  }
}

class RepeatUntilFailureMaxAttemptsSpec : Specification() {
  @RepeatUntilFailure(maxAttempts = 3)
  fun test() {
    expect
    true
  }
}

class RepeatUntilFailureSkipsOthersSpec : Specification() {
  fun `other test 1`() {
    expect
    true
  }

  @RepeatUntilFailure(maxAttempts = 1)
  fun `other test 2`() {
    expect
    true
  }

  @RepeatUntilFailure(maxAttempts = 1)
  fun test() {
    expect
    true
  }

  fun `other test 3`() {
    expect
    true
  }
}

class RepeatUntilFailureAbortedSpec : Specification() {
  @Shared var count = 0

  @RepeatUntilFailure(maxAttempts = 3)
  fun test() {
    given
    count++
    if (count == 2) {
      throw TestAbortedException("abort")
    }

    expect
    true
  }
}
