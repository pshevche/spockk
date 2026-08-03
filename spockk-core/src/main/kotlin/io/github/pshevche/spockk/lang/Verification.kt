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

@file:Suppress("unused")

package io.github.pshevche.spockk.lang

import org.opentest4j.MultipleFailuresError
import org.spockframework.runtime.SpockAssertionError

fun <T> verify(target: T, block: T.() -> Unit) = target.block()

fun verifyAll(block: () -> Unit) = block()

fun <T> verifyAll(target: T, block: T.() -> Unit) = target.block()

fun <T> verifyEach(things: Iterable<T>, block: T.() -> Unit) = verifyEach(things, { it.toString() }, block)

fun <T> verifyEach(things: Iterable<T>, namer: (T) -> String, block: T.() -> Unit) {
  val failures = mutableListOf<ItemFailure<T>>()
  var index = -1
  for (thing in things) {
    index++
    try {
      thing.block()
    } catch (throwable: Throwable) {
      failures += ItemFailure(thing, index, throwable)
    }
  }

  when {
    failures.size == 1 -> throw assertionFailedError(namer, failures[0])
    failures.isNotEmpty() -> throw MultipleFailuresError("", failures.map { assertionFailedError(namer, it) })
  }
}

private class ItemFailure<T>(val item: T, val index: Int, val throwable: Throwable)

private fun <T> assertionFailedError(namer: (T) -> String, failure: ItemFailure<T>): SpockAssertionError {
  val error = SpockAssertionError("Assertions failed for item[${failure.index}] ${namer(failure.item)}:\n${failure.throwable}")
  error.stackTrace = failure.throwable.stackTrace
  return error
}
