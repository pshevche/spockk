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

package io.github.pshevche.spockk.fixtures.runtime.samples.parallel

import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.spockframework.runtime.model.parallel.ExecutionMode
import org.spockframework.runtime.model.parallel.ResourceAccessMode
import spock.lang.Execution
import spock.lang.Isolated
import spock.lang.ResourceLock
import spock.lang.Shared
import spock.lang.Specification
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicInteger

fun incrementBlockAndCheck(sharedResource: AtomicInteger, latch: CountDownLatch): Boolean {
  val value = incrementAndBlock(sharedResource, latch)
  return value == sharedResource.get()
}

fun incrementAndBlock(sharedResource: AtomicInteger, latch: CountDownLatch, timeout: Long = 100): Int {
  val value = sharedResource.incrementAndGet()
  latch.countDown()
  latch.await(timeout, MILLISECONDS)
  return value
}

fun storeAndBlockAndCheck(sharedResource: AtomicInteger, latch: CountDownLatch, timeout: Long = 100): Boolean {
  val value = sharedResource.get()
  latch.countDown()
  latch.await(timeout, MILLISECONDS)
  return value == sharedResource.get()
}

object ParallelExecutionSpecs {
  class TestsExecuteInParallel : Specification() {
    @Shared val atomicInteger = AtomicInteger()

    @Shared val latch = CountDownLatch(2)

    fun a() {
      `when`
      incrementAndBlock(atomicInteger, latch, 10000)

      then
      atomicInteger.get() == 2
    }

    fun b() {
      `when`
      incrementAndBlock(atomicInteger, latch, 10000)

      then
      atomicInteger.get() == 2
    }
  }

  class ExclusiveResourceAccess : Specification() {
    @Shared val atomicInteger = AtomicInteger()

    @Shared val latch = CountDownLatch(2)

    @ResourceLock("a")
    fun writeA() {
      expect
      incrementBlockAndCheck(atomicInteger, latch)
    }

    @ResourceLock("a")
    fun writeB() {
      expect
      incrementBlockAndCheck(atomicInteger, latch)
    }
  }

  class SharedReadExclusiveWriteAccess : Specification() {
    @Shared val atomicInteger = AtomicInteger()

    @Shared val latch = CountDownLatch(3)

    @ResourceLock(value = "a", mode = ResourceAccessMode.READ)
    fun readA() {
      expect
      storeAndBlockAndCheck(atomicInteger, latch)
    }

    @ResourceLock("a")
    fun writeB() {
      expect
      incrementBlockAndCheck(atomicInteger, latch)
    }

    @ResourceLock(value = "a", mode = ResourceAccessMode.READ)
    fun readC() {
      expect
      storeAndBlockAndCheck(atomicInteger, latch)
    }
  }

  @ResourceLock("a")
  class WriteLockOnSpec : Specification() {
    @Shared val atomicInteger = AtomicInteger()

    @Shared val latch = CountDownLatch(2)

    @Shared var threadName = ""

    fun setupSpec() {
      threadName = Thread.currentThread().name
    }

    fun writeA() {
      expect
      threadName == Thread.currentThread().name
      incrementBlockAndCheck(atomicInteger, latch)
    }

    fun writeB() {
      expect
      threadName == Thread.currentThread().name
      incrementBlockAndCheck(atomicInteger, latch)
    }
  }

  @ResourceLock("a")
  abstract class ResourceLockBaseSpec : Specification() {
    @Shared val atomicInteger = AtomicInteger()

    @Shared val latch = CountDownLatch(2)
  }

  class InheritedResourceLock : ResourceLockBaseSpec() {
    @Shared var threadName = ""

    fun setupSpec() {
      threadName = Thread.currentThread().name
    }

    fun writeA() {
      expect
      threadName == Thread.currentThread().name
      incrementBlockAndCheck(atomicInteger, latch)
    }

    fun writeB() {
      expect
      threadName == Thread.currentThread().name
      incrementBlockAndCheck(atomicInteger, latch)
    }
  }

  @ResourceLock(value = "a", mode = ResourceAccessMode.READ)
  class ReadOnlyResourceLock : Specification() {
    @Shared val atomicInteger = AtomicInteger()

    @Shared val latch = CountDownLatch(2)

    fun writeA() {
      `when`
      incrementAndBlock(atomicInteger, latch)

      then
      atomicInteger.get() == 2
    }

    fun writeB() {
      `when`
      incrementAndBlock(atomicInteger, latch)

      then
      atomicInteger.get() == 2
    }
  }

  @ResourceLock(value = "a", mode = ResourceAccessMode.READ)
  class SpecLockCombinedWithFeatureLock : Specification() {
    @Shared val atomicInteger = AtomicInteger()

    @Shared val latch = CountDownLatch(2)

    fun readA() {
      expect
      storeAndBlockAndCheck(atomicInteger, latch)
    }

    @ResourceLock("a")
    fun writeB() {
      expect
      incrementBlockAndCheck(atomicInteger, latch)
    }

    fun readC() {
      expect
      storeAndBlockAndCheck(atomicInteger, latch)
    }
  }

  class ReadOnlyLockOnDataDrivenFeature : Specification() {
    @Shared val atomicInteger = AtomicInteger()

    @Shared val latch = CountDownLatch(3)

    @ResourceLock(value = "a", mode = ResourceAccessMode.READ)
    fun writeA(i: Int) {
      `when`
      incrementAndBlock(atomicInteger, latch)

      then
      atomicInteger.get() == 3

      where
      variable(i).from(1, 2, 3)
    }
  }

  class WriteLockOnDataDrivenFeature : Specification() {
    @Shared val atomicInteger = AtomicInteger()

    @Shared val latch = CountDownLatch(2)

    @Shared var threadName = ""

    fun setupSpec() {
      threadName = Thread.currentThread().name
    }

    @ResourceLock("a")
    fun writeA(i: Int) {
      expect
      threadName == Thread.currentThread().name
      incrementBlockAndCheck(atomicInteger, latch)

      where
      variable(i).from(1, 2)
    }
  }

  @Execution(ExecutionMode.SAME_THREAD)
  class SameThreadExecution : Specification() {
    @Shared val atomicInteger = AtomicInteger()

    @Shared val latch = CountDownLatch(2)

    @Shared var threadName = ""

    fun setupSpec() {
      threadName = Thread.currentThread().name
    }

    fun writeA() {
      expect
      threadName == Thread.currentThread().name
      incrementBlockAndCheck(atomicInteger, latch)
    }

    fun writeB() {
      expect
      threadName == Thread.currentThread().name
      incrementBlockAndCheck(atomicInteger, latch)
    }
  }

  @Execution(ExecutionMode.SAME_THREAD)
  abstract class SameThreadBaseSpec : Specification() {
    @Shared val atomicInteger = AtomicInteger()

    @Shared val latch = CountDownLatch(2)
  }

  class InheritedSameThreadExecution : SameThreadBaseSpec() {
    @Shared var threadName = ""

    fun setupSpec() {
      threadName = Thread.currentThread().name
    }

    fun writeA() {
      expect
      threadName == Thread.currentThread().name
      incrementBlockAndCheck(atomicInteger, latch)
    }

    fun writeB() {
      expect
      threadName == Thread.currentThread().name
      incrementBlockAndCheck(atomicInteger, latch)
    }
  }
}

object IsolatedExecutionSpecs {
  val atomicInteger = AtomicInteger()
  val latch = CountDownLatch(6)

  class ASpec : Specification() {
    fun writeA() {
      expect
      storeAndBlockAndCheck(atomicInteger, latch)
    }

    fun writeB() {
      expect
      storeAndBlockAndCheck(atomicInteger, latch)
    }
  }

  @Isolated
  class IsolatedBSpec : Specification() {
    @Shared var threadName = ""

    fun setupSpec() {
      threadName = Thread.currentThread().name
    }

    fun writeA() {
      expect
      threadName == Thread.currentThread().name
      incrementBlockAndCheck(atomicInteger, latch)
    }

    fun writeB() {
      expect
      threadName == Thread.currentThread().name
      incrementBlockAndCheck(atomicInteger, latch)
    }
  }

  class CSpec : Specification() {
    fun writeA() {
      expect
      storeAndBlockAndCheck(atomicInteger, latch)
    }

    fun writeB() {
      expect
      storeAndBlockAndCheck(atomicInteger, latch)
    }
  }
}

object InheritedIsolatedSpecs {
  val atomicInteger = AtomicInteger()
  val latch = CountDownLatch(6)

  class ASpec : Specification() {
    fun writeA() {
      expect
      storeAndBlockAndCheck(atomicInteger, latch)
    }

    fun writeB() {
      expect
      storeAndBlockAndCheck(atomicInteger, latch)
    }
  }

  @Isolated
  abstract class IsolatedBaseSpec : Specification()

  class BSpec : IsolatedBaseSpec() {
    @Shared var threadName = ""

    fun setupSpec() {
      threadName = Thread.currentThread().name
    }

    fun writeA() {
      expect
      threadName == Thread.currentThread().name
      incrementBlockAndCheck(atomicInteger, latch)
    }

    fun writeB() {
      expect
      threadName == Thread.currentThread().name
      incrementBlockAndCheck(atomicInteger, latch)
    }
  }

  class CSpec : Specification() {
    fun writeA() {
      expect
      storeAndBlockAndCheck(atomicInteger, latch)
    }

    fun writeB() {
      expect
      storeAndBlockAndCheck(atomicInteger, latch)
    }
  }
}
