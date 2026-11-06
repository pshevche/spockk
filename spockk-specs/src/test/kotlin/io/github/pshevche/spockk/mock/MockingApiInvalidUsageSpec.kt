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
import spock.lang.Specification
import spock.mock.MockMakers
import spock.mock.MockingApi
import java.lang.reflect.InvocationTargetException

class MockingApiInvalidUsageSpec : Specification() {

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
    "Mock"       ; listOf<Any?>(Runnable::class.java, CLOSURE)
    "Mock"       ; listOf<Any?>(OPTS)
    "Mock"       ; listOf<Any?>(OPTS, Runnable::class.java)
    "Mock"       ; listOf<Any?>(OPTS, Runnable::class.java, CLOSURE)
    "Mock"       ; listOf<Any?>(OPTS, CLOSURE)
    "Mock"       ; listOf<Any?>(CLOSURE)

    "Stub"       ; listOf<Any?>()
    "Stub"       ; listOf<Any?>(Runnable::class.java)
    "Stub"       ; listOf<Any?>(Runnable::class.java, CLOSURE)
    "Stub"       ; listOf<Any?>(OPTS)
    "Stub"       ; listOf<Any?>(OPTS, Runnable::class.java)
    "Stub"       ; listOf<Any?>(OPTS, Runnable::class.java, CLOSURE)
    "Stub"       ; listOf<Any?>(OPTS, CLOSURE)
    "Stub"       ; listOf<Any?>(CLOSURE)

    "Spy"        ; listOf<Any?>()
    "Spy"        ; listOf<Any?>(Runnable::class.java)
    "Spy"        ; listOf<Any?>(Runnable::class.java, CLOSURE)
    "Spy"        ; listOf<Any?>(OPTS)
    "Spy"        ; listOf<Any?>(OPTS, Runnable::class.java)
    "Spy"        ; listOf<Any?>(OPTS, Runnable::class.java, CLOSURE)
    "Spy"        ; listOf<Any?>(OPTS, CLOSURE)
    "Spy"        ; listOf<Any?>(CLOSURE)
    "Spy"        ; listOf<Any?>("Obj")
    "Spy"        ; listOf<Any?>("Obj", CLOSURE)

    "GroovyMock" ; listOf<Any?>()
    "GroovyMock" ; listOf<Any?>(Runnable::class.java)
    "GroovyMock" ; listOf<Any?>(Runnable::class.java, CLOSURE)
    "GroovyMock" ; listOf<Any?>(OPTS)
    "GroovyMock" ; listOf<Any?>(OPTS, Runnable::class.java)
    "GroovyMock" ; listOf<Any?>(OPTS, Runnable::class.java, CLOSURE)
    "GroovyMock" ; listOf<Any?>(OPTS, CLOSURE)
    "GroovyMock" ; listOf<Any?>(CLOSURE)

    "GroovyStub" ; listOf<Any?>()
    "GroovyStub" ; listOf<Any?>(Runnable::class.java)
    "GroovyStub" ; listOf<Any?>(Runnable::class.java, CLOSURE)
    "GroovyStub" ; listOf<Any?>(OPTS)
    "GroovyStub" ; listOf<Any?>(OPTS, Runnable::class.java)
    "GroovyStub" ; listOf<Any?>(OPTS, Runnable::class.java, CLOSURE)
    "GroovyStub" ; listOf<Any?>(OPTS, CLOSURE)
    "GroovyStub" ; listOf<Any?>(CLOSURE)

    "GroovySpy"  ; listOf<Any?>()
    "GroovySpy"  ; listOf<Any?>(Runnable::class.java)
    "GroovySpy"  ; listOf<Any?>(Runnable::class.java, CLOSURE)
    "GroovySpy"  ; listOf<Any?>(OPTS)
    "GroovySpy"  ; listOf<Any?>(OPTS, Runnable::class.java)
    "GroovySpy"  ; listOf<Any?>(OPTS, Runnable::class.java, CLOSURE)
    "GroovySpy"  ; listOf<Any?>(OPTS, CLOSURE)
    "GroovySpy"  ; listOf<Any?>(CLOSURE)

    "SpyStatic"  ; listOf<Any?>(Runnable::class.java)
    "SpyStatic"  ; listOf<Any?>(Runnable::class.java, MockMakers.mockito)
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

  companion object {
    private val CLOSURE = object : Closure<Any?>(Any()) {}
    private val OPTS = mapOf<String, Any?>()
  }
}
