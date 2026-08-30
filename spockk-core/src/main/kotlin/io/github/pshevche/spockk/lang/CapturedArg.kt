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

/**
 * Holds an argument value recorded by [capture] once the interaction it's used in has matched a real
 * invocation. Mirrors MockK's `slot`/Mockito's `ArgumentCaptor` - Spock itself has no dedicated
 * capture primitive of its own.
 */
class CapturedArg<T> {
  private var backing: T? = null
  private var hasValue: Boolean = false

  /** The captured value. Throws [IllegalStateException] if nothing has been captured yet. */
  val captured: T
    get() {
      check(hasValue) { "No value captured yet" }
      @Suppress("UNCHECKED_CAST")
      return backing as T
    }

  // Called by the generated `addCodeArg` closure (see SpockkClosure) when the interaction it backs
  // matches a real invocation.
  internal fun set(value: T) {
    backing = value
    hasValue = true
  }
}

/** Creates an empty [CapturedArg] to be filled in later by [capture]. */
fun <T> slot(): CapturedArg<T> = CapturedArg()
