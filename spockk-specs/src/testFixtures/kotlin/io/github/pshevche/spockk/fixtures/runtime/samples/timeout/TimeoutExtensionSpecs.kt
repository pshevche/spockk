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

package io.github.pshevche.spockk.fixtures.runtime.samples.timeout

import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import spock.lang.Specification
import spock.lang.Timeout
import java.util.concurrent.TimeUnit.MILLISECONDS

private const val FOREVER = 99999999999L

class TwoInterruptAttemptsSpec : Specification() {
  @Timeout(value = 100, unit = MILLISECONDS)
  fun foo() {
    `when`
    Thread.sleep(FOREVER)

    then
    thrown(InterruptedException::class.java)

    `when`
    Thread.sleep(FOREVER)

    then
    thrown(InterruptedException::class.java)
  }
}

class ThreeInterruptAttemptsSpec : Specification() {
  @Timeout(value = 100, unit = MILLISECONDS)
  fun foo() {
    `when`
    Thread.sleep(FOREVER)

    then
    thrown(InterruptedException::class.java)

    `when`
    Thread.sleep(FOREVER)

    then
    thrown(InterruptedException::class.java)

    `when`
    Thread.sleep(FOREVER)

    then
    thrown(InterruptedException::class.java)
  }
}
