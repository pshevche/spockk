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
import spock.lang.Specification

class SimpleComparisonSpec : Specification() {
  fun `test`() {
    val x = 5
    val y = 10
    expect
    x > y
  }
}

class IntegerEqualitySpec : Specification() {
  fun `test`() {
    expect
    1 == 2
  }
}

class NullValueSpec : Specification() {
  fun `test`() {
    val x: String? = null
    expect
    x != null
  }
}

class StringValueSpec : Specification() {
  fun `test`() {
    val x = "hello"
    expect
    x == null
  }
}

class BooleanNegationSpec : Specification() {
  fun `test`() {
    val flag = true
    expect
    !flag
  }
}

class StringMethodCallSpec : Specification() {
  fun `test`() {
    val str = "hello"
    expect
    str.startsWith("xyz")
  }
}

class PropertyAccessSpec : Specification() {
  fun `test`() {
    val str = "hello"
    expect
    str.length == 3
  }
}

class ArithmeticExpressionSpec : Specification() {
  fun `test`() {
    val a = 1
    val b = 2
    expect
    a + b == 4
  }
}

class LogicalAndSpec : Specification() {
  fun `test`() {
    val left = "spockk".isEmpty()
    val right = "kotlin".isNotEmpty()
    expect
    left && right
  }
}

class LogicalOrSpec : Specification() {
  fun `test`() {
    val left = "spockk".isEmpty()
    val right = "kotlin".isEmpty()
    expect
    left || right
  }
}

class ListAccessSpec : Specification() {
  fun `test`() {
    val list = listOf(1, 2, 3)
    expect
    list[0] == 10
  }
}

class ChainedExpressionSpec : Specification() {
  fun `test`() {
    val list = listOf("abc", "def")
    expect
    list[0].length == 10
  }
}

class StringEqualitySpec : Specification() {
  fun `test`() {
    val x = "spockk"
    val y = "kotlin"
    expect
    x == y
  }
}
