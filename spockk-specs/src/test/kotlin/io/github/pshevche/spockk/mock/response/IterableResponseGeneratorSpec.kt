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

package io.github.pshevche.spockk.mock.response

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.mock.IMockInvocation
import org.spockframework.mock.response.IterableResponseGenerator
import org.spockframework.mock.runtime.StaticMockMethod
import org.spockframework.util.ReflectionUtil
import spock.lang.Specification

class IterableResponseGeneratorSpec : Specification() {

  val inv: IMockInvocation = Mock(IMockInvocation::class.java)

  @MigratedFrom("org.spockframework.mock.response.IterableResponseGeneratorSpec#iterate over non-empty list")
  fun `iterate over non-empty list`() {
    given
    val gen = IterableResponseGenerator(listOf(1, 2, 3))
    val method = ReflectionUtil.getMethodByName(Any::class.java, "hashCode")

    `when`
    val first = gen.getResponseSupplier(inv).get()
    val second = gen.getResponseSupplier(inv).get()
    val third = gen.getResponseSupplier(inv).get()
    val fourth = gen.getResponseSupplier(inv).get()

    then
    inv.getMethod() returns StaticMockMethod(method, Any::class.java)
    first == 1
    second == 2
    third == 3
    fourth == 3
  }

  @MigratedFrom("org.spockframework.mock.response.IterableResponseGeneratorSpec#iterate over empty list")
  fun `iterate over empty list`() {
    given
    val gen = IterableResponseGenerator(emptyList<Any>())
    val method = ReflectionUtil.getMethodByName(Any::class.java, "toString")

    `when`
    val first = gen.getResponseSupplier(inv).get()
    val second = gen.getResponseSupplier(inv).get()

    then
    inv.getMethod() returns StaticMockMethod(method, Any::class.java)
    first == null
    second == null
  }

  @MigratedFrom("org.spockframework.mock.response.IterableResponseGeneratorSpec#iterate over string")
  fun `iterate over string`() {
    given
    val gen = IterableResponseGenerator("abc")
    val method = ReflectionUtil.getMethodByName(Any::class.java, "toString")

    `when`
    val first = gen.getResponseSupplier(inv).get()
    val second = gen.getResponseSupplier(inv).get()
    val third = gen.getResponseSupplier(inv).get()
    val fourth = gen.getResponseSupplier(inv).get()

    then
    inv.getMethod() returns StaticMockMethod(method, Any::class.java)
    first == "a"
    second == "b"
    third == "c"
    fourth == "c"
  }
}
