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
import io.github.pshevche.spockk.lang.given
import spock.lang.Issue
import spock.lang.Specification
import java.util.Queue

@Issue("https://github.com/spockframework/spock/issues/357")
class ChainedResponseGenerators : Specification() {

  @MigratedFrom("org.spockframework.smoke.mock.ChainedResponseGenerators#implicit default response")
  fun `implicit default response`() {
    given
    val queue = Mock(Queue::class.java)

    expect
    queue.poll() == null
    queue.poll() == null
  }
}
