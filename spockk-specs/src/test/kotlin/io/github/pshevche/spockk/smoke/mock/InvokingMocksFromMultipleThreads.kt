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
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.mock.TooFewInvocationsError
import org.spockframework.mock.TooManyInvocationsError
import spock.lang.PendingFeature
import spock.lang.Specification
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class InvokingMocksFromMultipleThreads : Specification() {

  private val numThreads = 10
  private val list: MutableList<Int> = Mock(MutableList::class.java) as MutableList<Int>
  private val latch = CountDownLatch(numThreads)

  private companion object {
    const val WAIT_TIME_S = 200L
  }

  private fun runThreads(body: (threadId: Int, count: Int) -> Unit, onError: (Throwable) -> Unit = {}) {
    val executor: ExecutorService = Executors.newVirtualThreadPerTaskExecutor()
    try {
      repeat(numThreads) { threadId ->
        executor.submit {
          try {
            repeat(100) { count ->
              body(threadId, count)
              Thread.sleep(1)
            }
          } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
            onError(e)
          } finally {
            latch.countDown()
          }
        }
      }
      check(latch.await(WAIT_TIME_S, TimeUnit.SECONDS)) {
        "The test threads did not terminate in ${WAIT_TIME_S}s."
      }
    } finally {
      executor.shutdownNow()
    }
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/989")
  @MigratedFrom("org.spockframework.smoke.mock.InvokingMocksFromMultipleThreads#invoking a mock from multiple threads")
  fun `invoking a mock from multiple threads`() {
    `when`
    runThreads({ _, count -> list.add(count) })

    then
    for (count in 0 until 100) {
      numThreads * list.add(count)
    }
  }

  // Upstream expects TooManyInvocationsError; restore @FailsWith when the gap is fixed.
  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/989")
  @MigratedFrom(
    "org.spockframework.smoke.mock.InvokingMocksFromMultipleThreads#invoking a mock from multiple threads - too many invocations"
  )
  fun `invoking a mock from multiple threads - too many invocations`() {
    `when`
    runThreads({ threadId, count ->
      list.add(count)
      if (threadId == 0 && count == 99) list.add(count)
    }, { if (it !is TooManyInvocationsError) throw it })

    then
    for (count in 0 until 100) {
      numThreads * list.add(count)
    }
  }

  // Upstream expects TooFewInvocationsError; restore @FailsWith when the gap is fixed.
  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/989")
  @MigratedFrom(
    "org.spockframework.smoke.mock.InvokingMocksFromMultipleThreads#invoking a mock from multiple threads - too few invocations"
  )
  fun `invoking a mock from multiple threads - too few invocations`() {
    `when`
    runThreads({ threadId, count ->
      if (!(threadId == 0 && count == 99)) list.add(count)
    }, { if (it !is TooFewInvocationsError) throw it })

    then
    for (count in 0 until 100) {
      numThreads * list.add(count)
    }
  }
}
