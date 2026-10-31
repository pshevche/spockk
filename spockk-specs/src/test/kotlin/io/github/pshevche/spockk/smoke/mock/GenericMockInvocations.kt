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
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.mock.IDefaultResponse
import org.spockframework.mock.IMockInvocation
import org.spockframework.mock.MockUtil
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.mock.DetachedMockFactory
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

class GenericMockInvocations : Specification() {

  interface Function<D, C> {
    fun get(value: D): C
  }

  open class StringIntFunction : Function<String, Int?> {
    override fun get(value: String): Int? = null
  }

  open class FunctionClass<D, C> {
    open fun get(value: D): C? = null
  }

  open class StringIntFunction2 : FunctionClass<String, Int>()

  open class GenericMethod {
    open fun <T> doIt(type: Class<T>): T? = null
  }

  private val factory = DetachedMockFactory()

  private val mockUtil = MockUtil()

  private var invocation: IMockInvocation? = null

  private val capture = object : IDefaultResponse {
    override fun respond(invocation: IMockInvocation): Any? {
      this@GenericMockInvocations.invocation = invocation
      return null
    }
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.GenericMockInvocations#mock with parameterized type")
  @OptIn(ExperimentalStdlibApi::class)
  fun `mock with parameterized type`() {
    given
    val holder: Function<String, Int> =
      Mock(mapOf("type" to typeOf<Function<String, Int>>().javaType, "defaultResponse" to capture))

    `when`
    holder.get("foo")

    then
    1 * holder.get(any())
    invocation!!.method.parameterTypes.toList() == listOf(String::class.java)
    invocation!!.method.returnType == Integer::class.java
  }

  @MigratedFrom("org.spockframework.smoke.mock.GenericMockInvocations#mock with raw type")
  fun `mock with raw type`() {
    given
    @Suppress("UNCHECKED_CAST")
    val holder = factory.Mock(mapOf("defaultResponse" to capture), Function::class.java) as Function<String, Any?>
    mockUtil.attachMock(holder, this)

    `when`
    holder.get("foo")

    then
    1 * holder.get(any())
    invocation!!.method.parameterTypes.toList() == listOf(Any::class.java)
    invocation!!.method.returnType == Any::class.java
    invocation!!.method.exactParameterTypes.toList() == listOf<Any>(Any::class.java)
    invocation!!.method.exactReturnType == Any::class.java
  }

  @MigratedFrom("org.spockframework.smoke.mock.GenericMockInvocations#mock whose type implements parameterized interface")
  fun `mock whose type implements parameterized interface`() {
    given
    val holder = factory.Mock(mapOf("defaultResponse" to capture), StringIntFunction::class.java)
    mockUtil.attachMock(holder, this)

    `when`
    holder.get("foo")

    then
    1 * holder.get(any())
    invocation!!.method.parameterTypes.toList() == listOf(String::class.java)
    invocation!!.method.returnType == Integer::class.java
  }

  @MigratedFrom("org.spockframework.smoke.mock.GenericMockInvocations#mock whose type extends parameterized class")
  fun `mock whose type extends parameterized class`() {
    given
    val holder = factory.Mock(mapOf("defaultResponse" to capture), StringIntFunction2::class.java)
    mockUtil.attachMock(holder, this)

    `when`
    holder.get("foo")

    then
    1 * holder.get(any())
    invocation!!.method.parameterTypes.toList() == listOf(String::class.java)
    invocation!!.method.returnType == Integer::class.java
  }

  @MigratedFrom("org.spockframework.smoke.mock.GenericMockInvocations#mock with generic method")
  fun `mock with generic method`() {
    given
    val generic = factory.Mock(mapOf("defaultResponse" to capture), GenericMethod::class.java)
    mockUtil.attachMock(generic, this)

    `when`
    generic.doIt(String::class.java)

    then
    1 * generic.doIt<String>(any())
    invocation!!.method.returnType == Any::class.java
    invocation!!.method.exactReturnType == Any::class.java
  }
}
