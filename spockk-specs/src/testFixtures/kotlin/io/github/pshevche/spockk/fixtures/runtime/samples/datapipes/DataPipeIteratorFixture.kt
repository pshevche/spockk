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

package io.github.pshevche.spockk.fixtures.runtime.samples.datapipes

import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.where
import spock.lang.Specification

/**
 * Mirrors upstream's ThreadLocal-backed `currentDataProvider`/`dataProvider()`/`getDataProvider()`: the data
 * source is swapped between runs of [DataPipeIteratorFixture] rather than baked into it.
 */
object CurrentDataProvider {
  private val holder = ThreadLocal<Any>()

  fun <T : Any> set(provider: T): T {
    holder.set(provider)
    return provider
  }

  @Suppress("UNCHECKED_CAST")
  fun get(): List<Any?> = holder.get() as List<Any?>
}

class DataPipeIteratorFixture : Specification() {
  fun `single data pipe feature`(input: Any?) {
    expect
    input != null

    where
    variable(input).from(CurrentDataProvider.get())
  }
}

class TestDataCollection : AbstractCollection<Any?>() {
  var iteratorCalls = 0
  var sizeCalls = 0

  override fun iterator(): MutableIterator<Any?> {
    iteratorCalls++
    return mutableListOf<Any?>("Value").iterator()
  }

  override val size: Int
    get() {
      sizeCalls++
      return 1
    }
}

class TestDataIterableWithSize : Iterable<Any?> {
  var iteratorCalls = 0
  var sizeCalls = 0

  fun size(): Int {
    sizeCalls++
    return 1
  }

  override fun iterator(): Iterator<Any?> {
    iteratorCalls++
    return listOf<Any?>("Value").iterator()
  }
}

class TestDataIterable : Iterable<Any?> {
  var iteratorCalls = 0

  override fun iterator(): Iterator<Any?> {
    iteratorCalls++
    return listOf<Any?>("Value").iterator()
  }
}

class TestDataIterator : Iterator<Any?> {
  var hasNextCalls = 0
  var nextCalls = 0

  override fun hasNext(): Boolean {
    hasNextCalls++
    return nextCalls == 0
  }

  override fun next(): Any? {
    nextCalls++
    if (nextCalls == 1) return "Value"
    throw NoSuchElementException()
  }
}
