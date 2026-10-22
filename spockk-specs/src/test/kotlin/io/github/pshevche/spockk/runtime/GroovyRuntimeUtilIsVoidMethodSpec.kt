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

package io.github.pshevche.spockk.runtime

import groovy.lang.Closure
import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import org.codehaus.groovy.runtime.GStringImpl
import org.spockframework.runtime.GroovyRuntimeUtil.isVoidMethod
import spock.lang.Specification
import java.util.Collections

@Suppress("unused")
internal class KotlinMethods {
  fun voidInstanceMethod() {}

  fun nonVoidInstanceMethod(): Int = 1

  fun untypedInstanceMethodWithRetVal(): Any? = 1

  fun untypedInstanceMethodWithoutRetVal(): Any? = null

  fun typedInstanceMethodWithRetVal(): String = "x"

  fun typedInstanceMethodWithoutRetVal(): String? = null

  fun methodWithStringParam(@Suppress("UNUSED_PARAMETER") value: String): String = value

  companion object {
    @JvmStatic
    fun voidStaticMethod() {}

    @JvmStatic
    fun nonVoidStaticMethod(): Int = 1

    @JvmStatic
    fun untypedStaticMethodWithRetVal(): Any? = 1

    @JvmStatic
    fun untypedStaticMethodWithoutRetVal(): Any? = null

    @JvmStatic
    fun typedStaticMethodWithRetVal(): String = "x"

    @JvmStatic
    fun typedStaticMethodWithoutRetVal(): String? = null
  }
}

class GroovyRuntimeUtilIsVoidMethodSpec : Specification() {

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilIsVoidMethodSpec#void Java instance method")
  fun `void Java instance method`() {
    expect
    isVoidMethod(KotlinMethods(), "voidInstanceMethod")
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilIsVoidMethodSpec#non-void Java instance method")
  fun `non-void Java instance method`() {
    expect
    !isVoidMethod(KotlinMethods(), "nonVoidInstanceMethod")
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilIsVoidMethodSpec#void Java static method")
  fun `void Java static method`() {
    expect
    isVoidMethod(KotlinMethods(), "voidStaticMethod")
    isVoidMethod(KotlinMethods::class.java, "voidStaticMethod")
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilIsVoidMethodSpec#non-void Java static method")
  fun `non-void Java static method`() {
    expect
    !isVoidMethod(KotlinMethods(), "nonVoidStaticMethod")
    !isVoidMethod(KotlinMethods::class.java, "nonVoidStaticMethod")
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilIsVoidMethodSpec#void Groovy instance method")
  fun `void Groovy instance method`() {
    expect
    isVoidMethod(KotlinMethods(), "voidInstanceMethod")
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilIsVoidMethodSpec#untyped Groovy instance method")
  fun `untyped Groovy instance method`() {
    expect
    !isVoidMethod(KotlinMethods(), "untypedInstanceMethodWithRetVal")
    !isVoidMethod(KotlinMethods(), "untypedInstanceMethodWithoutRetVal")
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilIsVoidMethodSpec#typed Groovy instance method")
  fun `typed Groovy instance method`() {
    expect
    !isVoidMethod(KotlinMethods(), "typedInstanceMethodWithRetVal")
    !isVoidMethod(KotlinMethods(), "typedInstanceMethodWithoutRetVal")
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilIsVoidMethodSpec#void Groovy static method")
  fun `void Groovy static method`() {
    expect
    isVoidMethod(KotlinMethods(), "voidStaticMethod")
    isVoidMethod(KotlinMethods::class.java, "voidStaticMethod")
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilIsVoidMethodSpec#untyped Groovy static method")
  fun `untyped Groovy static method`() {
    expect
    !isVoidMethod(KotlinMethods(), "untypedStaticMethodWithRetVal")
    !isVoidMethod(KotlinMethods::class.java, "untypedStaticMethodWithRetVal")
    !isVoidMethod(KotlinMethods(), "untypedStaticMethodWithoutRetVal")
    !isVoidMethod(KotlinMethods::class.java, "untypedStaticMethodWithoutRetVal")
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilIsVoidMethodSpec#typed Groovy static method")
  fun `typed Groovy static method`() {
    expect
    !isVoidMethod(KotlinMethods(), "typedStaticMethodWithRetVal")
    !isVoidMethod(KotlinMethods::class.java, "typedStaticMethodWithRetVal")
    !isVoidMethod(KotlinMethods(), "typedStaticMethodWithoutRetVal")
    !isVoidMethod(KotlinMethods::class.java, "typedStaticMethodWithoutRetVal")
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilIsVoidMethodSpec#non-void java.lang.Class method")
  fun `non-void java_lang_Class method`() {
    expect
    !isVoidMethod(Int::class.javaPrimitiveType, "getSuperclass")
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilIsVoidMethodSpec#void java.lang.Class method")
  fun `void java_lang_Class method`() {
    expect
    isVoidMethod(Collections::class.java, "notify")
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilIsVoidMethodSpec#void DGM method")
  fun `void DGM method`() {
    expect
    isVoidMethod(this, "print", "")
    isVoidMethod(3, "times", Closure.IDENTITY)
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilIsVoidMethodSpec#non-void DGM method")
  fun `non-void DGM method`() {
    expect
    !isVoidMethod("a", "find", "b")
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilIsVoidMethodSpec#void DGSM method")
  fun `void DGSM method`() {
    expect
    isVoidMethod(this, "sleep", 1)
    isVoidMethod(Any::class.java, "sleep", 1)
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilIsVoidMethodSpec#non-void DGSM method")
  fun `non-void DGSM method`() {
    expect
    !isVoidMethod(Thread::class.java, "start", Closure.IDENTITY)
  }

  @MigratedFrom(
    "org.spockframework.runtime.GroovyRuntimeUtilIsVoidMethodSpec#Groovy method that takes a String with a GString"
  )
  fun `Groovy method that takes a String with a GString`() {
    expect
    !isVoidMethod(
      KotlinMethods(),
      "methodWithStringParam",
      GStringImpl(arrayOf<Any?>("two"), arrayOf("one ", " three"))
    )
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilIsVoidMethodSpec#method with null target")
  fun `method with null target`() {
    expect
    !isVoidMethod(null, "foo", 1)
  }
}
