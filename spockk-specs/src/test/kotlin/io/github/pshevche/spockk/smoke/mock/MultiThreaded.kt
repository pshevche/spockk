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
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.did
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.lang.Timeout
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.concurrent.thread

interface IndexedLookup {
  fun get(index: Int): Any?
}

class MultiThreaded : Specification() {

  @Issue("https://github.com/spockframework/spock/issues/583")
  @MigratedFrom("org.spockframework.smoke.mock.MultiThreaded#mocks can block and wait on conditions")
  fun `mocks can block and wait on conditions`() {
    given
    val latch1 = CountDownLatch(1)
    val latch2 = CountDownLatch(1)
    val latch3 = CountDownLatch(2)
    val aList = Mock(IndexedLookup::class.java)

    `when`
    thread {
      aList.get(0)
      latch3.countDown()
    }
    thread {
      aList.get(0)
      latch3.countDown()
    }
    assert(latch1.await(30, SECONDS))
    assert(latch2.await(30, SECONDS))

    then
    latch3.await(10, SECONDS)

    1 * aList.get(any()) did {
      latch1.countDown()
      assert(latch2.await(10, SECONDS))
      null
    }

    1 * aList.get(any()) did {
      latch2.countDown()
      assert(latch1.await(10, SECONDS))
      null
    }
  }

  @Issue("https://github.com/spockframework/spock/issues/1899")
  @Timeout(30)
  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.MultiThreaded#mock response generators do not deadlock when self-referencing")
  fun `mock response generators do not deadlock when self-referencing`() {
    given
    var called = false
    val foo = Mock(Runnable::class.java)
    foo.run() did {
      if (!called) {
        called = true
        thread { foo.run() }.join()
      }
    }

    `when`
    foo.run()

    then
    noExceptionThrown()
  }
}
