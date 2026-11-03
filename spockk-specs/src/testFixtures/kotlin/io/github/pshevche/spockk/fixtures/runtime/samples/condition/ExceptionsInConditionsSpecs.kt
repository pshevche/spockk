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

package io.github.pshevche.spockk.fixtures.runtime.samples.condition

import io.github.pshevche.spockk.lang.expect
import org.spockframework.runtime.SpockException
import spock.lang.Specification

class NullPointerInConditionSpec : Specification() {
  fun `a feature`() {
    val map = HashMap<String, String>()
    expect
    map.get("key")!!.length == 0
  }
}

class NullPointerInMethodConditionSpec : Specification() {
  fun `a feature`() {
    val map = HashMap<String, String>()
    expect
    map.get("key")!!.isEmpty()
  }
}

class ExceptionWhileInvokingConditionSpec : Specification() {
  fun `a feature`() {
    val getKey = { map: Map<String, String>, key: String ->
      val result = map.get(key)
      if (result == null) {
        throw IllegalArgumentException("key does not exists")
      }
      result
    }
    val map = HashMap<String, String>()
    expect
    getKey(map, "key").length == 0
  }
}

class NestedAssertionErrorSpec : Specification() {
  fun `a feature`() {
    val getKey = { map: Map<String, String>, key: String ->
      val result = map.get(key)
      assert(result != null)
      result!!
    }
    val map = HashMap<String, String>()
    expect
    getKey(map, "key").length == 0
  }
}

class SpockExceptionInConditionSpec : Specification() {
  fun `a feature`() {
    val getKey = { map: Map<String, String>, key: String ->
      val result = map.get(key)
      if (result == null) {
        throw SpockException("key does not exists")
      }
      result
    }
    val map = HashMap<String, String>()
    expect
    getKey(map, "key").length == 0
  }
}

class DeepNode {
  val b: DeepNode get() = this
  val c: DeepNode get() = this
  val d: DeepNode get() = this

  fun e(): Boolean = true
}

class DeepNestingSpec : Specification() {
  fun `a feature`() {
    val map = HashMap<String, DeepNode>()
    expect
    map.get("key")!!.b.c.d.e()
  }
}
