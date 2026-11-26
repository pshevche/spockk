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

package io.github.pshevche.spockk.mock

import groovy.lang.Closure
import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.spockframework.runtime.InvalidSpecException
import spock.lang.Shared
import spock.lang.Specification
import spock.mock.MockingApi
import java.lang.reflect.InvocationTargetException

class MockingApiInvalidUsageSpec : Specification() {

  @Shared
  private val closure: Closure<*> = object : Closure<Any?>(Any()) {}

  @Shared
  private val opts: Map<String, Any?> = mapOf()

  @MigratedFrom("spock.mock.MockingApiInvalidUsageSpec#Invalid usage check")
  fun `Invalid usage check`(methodName: String, args: List<Any?>) {
    `when`
    val api = MockingApi()
    invokeMethod(api, methodName, args)

    then
    val ex = thrown(InvalidSpecException::class.java)
    ex.message == "Mock objects can only be created inside a spec, and only during the lifetime of a feature (iteration)"

    where
    methodName   ; args
    "Mock"       ; listOf<Any?>()
    "Mock"       ; listOf<Any?>(Runnable::class.java)
    "Mock"       ; listOf<Any?>(Runnable::class.java, closure)
    "Mock"       ; listOf<Any?>(opts)
    "Mock"       ; listOf<Any?>(opts, Runnable::class.java)
    "Mock"       ; listOf<Any?>(opts, Runnable::class.java, closure)
    "Mock"       ; listOf<Any?>(opts, closure)
    "Mock"       ; listOf<Any?>(closure)

    "Stub"       ; listOf<Any?>()
    "Stub"       ; listOf<Any?>(Runnable::class.java)
    "Stub"       ; listOf<Any?>(Runnable::class.java, closure)
    "Stub"       ; listOf<Any?>(opts)
    "Stub"       ; listOf<Any?>(opts, Runnable::class.java)
    "Stub"       ; listOf<Any?>(opts, Runnable::class.java, closure)
    "Stub"       ; listOf<Any?>(opts, closure)
    "Stub"       ; listOf<Any?>(closure)

    "Spy"        ; listOf<Any?>()
    "Spy"        ; listOf<Any?>(Runnable::class.java)
    "Spy"        ; listOf<Any?>(Runnable::class.java, closure)
    "Spy"        ; listOf<Any?>(opts)
    "Spy"        ; listOf<Any?>(opts, Runnable::class.java)
    "Spy"        ; listOf<Any?>(opts, Runnable::class.java, closure)
    "Spy"        ; listOf<Any?>(opts, closure)
    "Spy"        ; listOf<Any?>(closure)
    "Spy"        ; listOf<Any?>("Obj")
    "Spy"        ; listOf<Any?>("Obj", closure)
  }

  // Picks the most specific overload by runtime argument types, like Groovy's dynamic dispatch.
  private fun invokeMethod(api: MockingApi, name: String, args: List<Any?>) {
    val method = MockingApi::class.java.methods
      .filter { it.name == name && it.parameterCount == args.size }
      .filter { m -> m.parameterTypes.indices.all { m.parameterTypes[it].isInstance(args[it]) } }
      .minWithOrNull(compareBy { m -> m.parameterTypes.count { it == Any::class.java } })
      ?: error("No applicable overload for $name($args)")
    try {
      method.invoke(api, *args.toTypedArray())
    } catch (e: InvocationTargetException) {
      throw e.targetException
    }
  }
}
