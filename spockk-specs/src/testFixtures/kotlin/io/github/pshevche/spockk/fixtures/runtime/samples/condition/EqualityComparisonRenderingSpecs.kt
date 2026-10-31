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
import java.io.Serializable
import java.util.LinkedList

class DifferentRepresentationsSpec : Specification() {
  fun `test`() {
    val x = 1
    val y = 2
    expect
    x == y
  }
}

class EqualityPerson(val name: String) {
  override fun toString(): String = name

  override fun equals(other: Any?): Boolean = false

  override fun hashCode(): Int = name.hashCode()
}

class SameRepresentationsSameTypesSpec : Specification() {
  fun `test`() {
    val x = EqualityPerson("Fred")
    val y = EqualityPerson("Fred")
    expect
    x == y
  }
}

class SameRepresentationsDifferentArrayTypesSpec : Specification() {
  fun `test`() {
    val x: Any = intArrayOf(1)
    val y: Any = arrayOf("1")
    expect
    x == y
  }
}

class SameRepresentationsDifferentAnonymousTypesSpec : Specification() {
  fun `test`() {
    val x: Any = object : Serializable {
      override fun toString() = "foo"
    }
    val y: Any = object : Serializable {
      override fun toString() = "foo"
    }
    expect
    x == y
  }
}

class SameRenderedAndLiteralRepresentationsSpec : Specification() {
  fun `test`() {
    val x: Any = "123"
    expect
    x == 123
  }
}

class SameLiteralRepresentationsSpec : Specification() {
  fun `test`() {
    expect
    arrayListOf(0, 1) == linkedSetOf(0, 1)
  }
}

class NullValuesSpec : Specification() {
  fun `test`() {
    val x: Any? = null
    val y: Any? = "null"
    expect
    x == y
  }
}

class NestedEqualityTypeHintsSpec : Specification() {
  fun `test`() {
    val x: Any = 1
    val y: Any = "1"
    expect
    ((x == y) as Any) is String
  }
}

class NestedEqualityEqualValuesSpec : Specification() {
  fun `test`() {
    val x: Any = arrayListOf(1)
    val y: Any = LinkedList(listOf(1))
    expect
    ((x == y) as Any) is String
  }
}

class EqualsMethodNoTypeHintsSpec : Specification() {
  fun `test`() {
    val x: Any = 1
    val y: Any = "1"
    expect
    x.equals(y)
  }
}
