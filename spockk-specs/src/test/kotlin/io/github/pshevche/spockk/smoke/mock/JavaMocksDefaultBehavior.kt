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
import io.github.pshevche.spockk.lang.and
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.does
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.returns
import spock.lang.PendingFeature
import spock.lang.Specification

class JavaMocksDefaultBehavior : Specification() {

  @MigratedFrom("org.spockframework.smoke.mock.JavaMocksDefaultBehavior#by default, a mock is only equal to itself")
  fun `by default, a mock is only equal to itself`() {
    val mock = Mock(Foo::class.java)
    val anotherMockOfSameType = Mock(Foo::class.java)
    val mockOfDifferentType = Mock(Bar::class.java)

    expect
    mock == mock
    mock != anotherMockOfSameType
    mock != mockOfDifferentType

    and
    mock.equals(mock)
    !mock.equals(anotherMockOfSameType)
    !mock.equals(mockOfDifferentType)
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.JavaMocksDefaultBehavior#by default, a Comparable mock is only equal to itself"
  )
  fun `by default, a Comparable mock is only equal to itself`() {
    val mock = Mock(Baz::class.java)
    val anotherMockOfSameType = Mock(Baz::class.java)
    val mockOfDifferentType = Mock(Qux::class.java)

    expect
    mock == mock
    mock != anotherMockOfSameType
    mock != mockOfDifferentType

    and
    mock.equals(mock)
    !mock.equals(anotherMockOfSameType)
    !mock.equals(mockOfDifferentType)
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.JavaMocksDefaultBehavior#by default, a mock returns System.identityHashCode() for its hash code"
  )
  fun `by default, a mock returns System-identityHashCode() for its hash code`() {
    val mock = Mock(Foo::class.java)

    expect
    mock.hashCode() == System.identityHashCode(mock)
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaMocksDefaultBehavior#default toString() output for named mock")
  fun `default toString() output for named mock`() {
    val myMock = Mock(Foo::class.java)

    expect
    myMock.toString() == "Mock for type 'Foo' named 'myMock'"
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaMocksDefaultBehavior#default toString() output for unnamed mock")
  fun `default toString() output for unnamed mock`() {
    expect
    Mock(Foo::class.java).toString() == "Mock for type 'Foo'"
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaMocksDefaultBehavior#default equals() behavior can be overridden")
  fun `default equals() behavior can be overridden`() {
    val bar = Mock(Bar::class.java) {
      equals(any()) returns false
    }
    val foo = Mock(Foo::class.java) {
      equals(any()) does { args -> args[0] === bar }
    }

    expect
    !foo.equals(foo)
    foo.equals(bar)
    !bar.equals(bar)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1001")
  @MigratedFrom("org.spockframework.smoke.mock.JavaMocksDefaultBehavior#default hashCode() behavior can be overridden")
  fun `default hashCode() behavior can be overridden`() {
    val foo = Mock(Foo::class.java)
    val bar = Mock(Bar::class.java)
    // Upstream stubs both with a wildcard target (`_.hashCode() >> 42`), which has no Kotlin spelling yet.

    expect
    foo.hashCode() == 42
    bar.hashCode() == 42
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaMocksDefaultBehavior#default toString() behavior can be overridden")
  fun `default toString() behavior can be overridden`() {
    val foo = Mock(Foo::class.java) {
      toString() returns "mock around the clock"
    }

    expect
    foo.toString() == "mock around the clock"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1001")
  @MigratedFrom("org.spockframework.smoke.mock.JavaMocksDefaultBehavior#default compareTo() behavior can be overridden")
  fun `default compareTo() behavior can be overridden`() {
    val baz = Mock(Baz::class.java)
    val qux = Mock(Qux::class.java)
    // Upstream stubs both with a wildcard target (`_.compareTo(_) >> -1`), which has no Kotlin spelling yet.

    expect
    baz.compareTo(qux) == -1
    baz.compareTo(baz) == -1
    qux.compareTo(baz) == -1
    qux.compareTo(qux) == -1
  }

  interface Foo

  interface Bar

  interface Baz : Comparable<Any>

  interface Qux : Comparable<Any>
}
