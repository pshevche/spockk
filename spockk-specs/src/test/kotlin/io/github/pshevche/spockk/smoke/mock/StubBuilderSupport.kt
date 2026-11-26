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
import io.github.pshevche.spockk.lang.Stub
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import spock.lang.Specification

class StubBuilderSupport : Specification() {

  @MigratedFrom("org.spockframework.smoke.mock.StubBuilderSupport#Stubs support builder pattern")
  fun `Stubs support builder pattern`() {
    given
    val builder = Stub(BuilderExample::class.java) {
      build() returns "world"
    }

    expect
    builder.withKey("foo").withValue("bar").build() == "world"
  }

  @MigratedFrom("org.spockframework.smoke.mock.StubBuilderSupport#builder pattern does not apply for methods returning Object")
  fun `builder pattern does not apply for methods returning Object`() {
    given
    val builder = Stub(BuilderExample::class.java) {
      build() returns "world"
    }

    expect
    builder.someMethodReturningObject().javaClass == Any::class.java
  }

  interface BuilderExample {
    fun withValue(v: String): BuilderExample

    fun withKey(v: String): BuilderExample

    fun build(): String

    fun someMethodReturningObject(): Any
  }
}
