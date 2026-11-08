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

package io.github.pshevche.spockk.smoke

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.smoke.VoidKotlinStaticMethodInThenBlocks.foo
import org.spockframework.runtime.model.parallel.Resources
import spock.lang.ResourceLock
import spock.lang.Specification
import java.util.Collections
import java.util.Collections.reverse

class VoidMethodCallsInThenBlocks : Specification() {

  @MigratedFrom("org.spockframework.smoke.VoidMethodCallsInThenBlocks#invocation of void Groovy instance method")
  fun `invocation of void Kotlin instance method`() {
    `when`("")

    then
    voidInstanceMethod()
    this.voidInstanceMethod()
  }

  @Suppress("RemoveRedundantQualifierName")
  @MigratedFrom("org.spockframework.smoke.VoidMethodCallsInThenBlocks#invocation of void Groovy static method")
  fun `invocation of void Kotlin static method`() {
    `when`("")

    then
    voidStaticMethod()
    VoidMethodCallsInThenBlocks.voidStaticMethod()
  }

  @MigratedFrom("org.spockframework.smoke.VoidMethodCallsInThenBlocks#invocation of void Java instance method")
  fun `invocation of void Java instance method`() {
    `when`("")

    then
    mutableListOf<Any>().clear()
  }

  @Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
  @MigratedFrom("org.spockframework.smoke.VoidMethodCallsInThenBlocks#invocation of void Java static method")
  fun `invocation of void Java static method`() {
    `when`("")

    then
    Collections.shuffle(mutableListOf<Any>())
  }

  @ResourceLock(Resources.SYSTEM_OUT)
  @MigratedFrom("org.spockframework.smoke.VoidMethodCallsInThenBlocks#invocation of void default Groovy method")
  fun `invocation of void default Kotlin method`() {
    `when`("")

    then
    print("")
  }

  @MigratedFrom("org.spockframework.smoke.VoidMethodCallsInThenBlocks#invocation of void default Groovy static method")
  fun `invocation of void default Kotlin static method`() {
    `when`("")

    then
    Thread.sleep(1)
    Thread.sleep(1)
  }

  @MigratedFrom("org.spockframework.smoke.VoidMethodCallsInThenBlocks#invocation of statically imported void Groovy method")
  fun `invocation of statically imported void Kotlin method`() {
    `when`("")

    then
    foo()
  }

  @Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
  @MigratedFrom("org.spockframework.smoke.VoidMethodCallsInThenBlocks#invocation of statically imported void Java method")
  fun `invocation of statically imported void Java method`() {
    `when`("")

    then
    reverse(mutableListOf<Any>())
  }

  private fun voidInstanceMethod() {}

  companion object {
    @JvmStatic
    fun voidStaticMethod() {}
  }
}

object VoidKotlinStaticMethodInThenBlocks {
  @JvmStatic
  fun foo() {}
}
