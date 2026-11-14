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
import io.github.pshevche.spockk.lang.Stub
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import spock.lang.Specification
import java.util.concurrent.CompletableFuture

class CompletableFutureSpec : Specification() {

  interface TestService {
    fun getFuture(): CompletableFuture<String?>
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.CompletableFutureSpec#default answer for CompletableFuture should be completed future"
  )
  fun `default answer for CompletableFuture should be completed future`() {
    given
    val service: TestService = Stub()

    `when`
    val receivedFuture = service.getFuture()

    then
    receivedFuture.isDone
    !receivedFuture.isCompletedExceptionally
    receivedFuture.get() == null
  }
}
