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
import io.github.pshevche.spockk.lang.does
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.returns
import spock.lang.PendingFeature
import spock.lang.Specification

class MockCreationWithClosure : Specification() {

  @MigratedFrom("org.spockframework.smoke.mock.MockCreationWithClosure#stub method")
  fun `stub method`() {
    val list = Mock(CreationList::class.java) {
      size() returns 42
    }

    expect
    list.size() == 42
  }

  @MigratedFrom("org.spockframework.smoke.mock.MockCreationWithClosure#stub property")
  fun `stub property`() {
    val list = Mock(CreationList::class.java) {
      isEmpty() returns false
    }

    expect
    !list.isEmpty()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1006")
  @MigratedFrom("org.spockframework.smoke.mock.MockCreationWithClosure#mock is declared in interaction")
  fun `mock is declared in interaction`() {
    val list = Mock(CreationList::class.java)
    list.get(any()) does { args ->
      val index = args[0] as Int
      Mock(CreationMap::class.java) {
        containsKey(any()) does { index % 2 == 0 }
      }
    }

    expect
    list.get(42).containsKey("foo")
    !list.get(21).containsKey("foo")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1006")
  @MigratedFrom("org.spockframework.smoke.mock.MockCreationWithClosure#nested mock declaration")
  fun `nested mock declaration`() {
    val list = Mock(CreationList::class.java) {
      get(any()) does { args ->
        val index = args[0] as Int
        Mock(CreationMap::class.java) {
          size() returns index + 1
        }
      }
    }

    expect
    list.get(21).size() == 22
    list.get(88).size() == 89
    list.get(21).size() == 22
    list.get(88).size() == 89
  }
}

interface CreationList {
  fun size(): Int

  fun isEmpty(): Boolean

  fun get(index: Int): CreationMap
}

interface CreationMap {
  fun size(): Int

  fun containsKey(key: String): Boolean
}
