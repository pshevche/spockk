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
  fun `test`() {
    val map = HashMap<String, String?>()
    expect
    map["key"]!!.length == 0
  }
}

class NullPointerInMethodConditionSpec : Specification() {
  fun `test`() {
    val map = HashMap<String, String?>()
    expect
    map["key"]!!.isEmpty()
  }
}

class ExceptionWhileInvokingConditionSpec : Specification() {
  fun `test`() {
    val getKey = { map: Map<String, String>, key: String ->
      map[key] ?: throw IllegalArgumentException("key does not exists")
    }
    val map = HashMap<String, String>()
    expect
    getKey(map, "key").length == 0
  }
}

class NestedAssertionErrorSpec : Specification() {
  fun `test`() {
    val getKey = { map: Map<String, String>, key: String ->
      val result = map[key]
      assert(result != null)
      result!!
    }
    val map = HashMap<String, String>()
    expect
    getKey(map, "key").length == 0
  }
}

class SpockExceptionInConditionSpec : Specification() {
  fun `test`() {
    val getKey = { map: Map<String, String>, key: String ->
      map[key] ?: throw SpockException("key does not exists")
    }
    val map = HashMap<String, String>()
    expect
    getKey(map, "key").length == 0
  }
}

class Node(val child: Node?, val name: String)

class DeepNestingSpec : Specification() {
  fun `test`() {
    val map = HashMap<String, Node?>()
    expect
    map["key"]!!.child!!.child!!.child!!.name.isEmpty()
  }
}
