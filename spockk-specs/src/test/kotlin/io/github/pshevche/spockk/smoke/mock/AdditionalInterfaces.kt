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
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import spock.lang.PendingFeature
import spock.lang.Specification
import java.io.Closeable

class AdditionalInterfaces : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.AdditionalInterfaces#java stubs")
  fun `java stubs`() {
    given
    val stub = Stub(mapOf<String, Any>("additionalInterfaces" to listOf(Closeable::class.java)), List::class.java)

    expect
    stub is List<*>
    stub is Closeable
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.AdditionalInterfaces#java mocks")
  fun `java mocks`() {
    given
    val mock = Mock(mapOf<String, Any>("additionalInterfaces" to listOf(Closeable::class.java)), List::class.java)

    expect
    mock is List<*>
    mock is Closeable
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.AdditionalInterfaces#java spies")
  fun `java spies`() {
    given
    val spy = Spy(mapOf<String, Any>("additionalInterfaces" to listOf(Closeable::class.java)), ArrayList::class.java)

    expect
    spy is List<*>
    spy is Closeable
  }
}
