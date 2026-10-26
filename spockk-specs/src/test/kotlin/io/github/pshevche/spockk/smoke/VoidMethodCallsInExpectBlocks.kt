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
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.smoke.VoidKotlinStaticMethod.foo
import spock.lang.Specification
import java.util.Collections
import java.util.Collections.reverse

class VoidMethodCallsInExpectBlocks : Specification() {

  @MigratedFrom("org.spockframework.smoke.VoidMethodCallsInExpectBlocks#invocation of void Groovy instance method")
  fun `invocation of void Groovy instance method`() {
    expect
    voidInstanceMethod()
    this.voidInstanceMethod()
  }

  @MigratedFrom("org.spockframework.smoke.VoidMethodCallsInExpectBlocks#invocation of void Groovy static method")
  fun `invocation of void Groovy static method`() {
    expect
    voidStaticMethod()
    VoidMethodCallsInExpectBlocks.voidStaticMethod()
  }

  @MigratedFrom("org.spockframework.smoke.VoidMethodCallsInExpectBlocks#invocation of void Java instance method")
  fun `invocation of void Java instance method`() {
    expect
    mutableListOf<Any>().clear()
  }

  @MigratedFrom("org.spockframework.smoke.VoidMethodCallsInExpectBlocks#invocation of void Java static method")
  fun `invocation of void Java static method`() {
    expect
    Collections.shuffle(mutableListOf<Any>())
  }

  @MigratedFrom("org.spockframework.smoke.VoidMethodCallsInExpectBlocks#invocation of statically imported void Java method")
  fun `invocation of statically imported void Java method`() {
    expect
    reverse(mutableListOf<Any>())
  }

  @MigratedFrom("org.spockframework.smoke.VoidMethodCallsInExpectBlocks#invocation of void default Groovy method")
  fun `invocation of void default Groovy method`() {
    expect
    print("")
  }

  @MigratedFrom("org.spockframework.smoke.VoidMethodCallsInExpectBlocks#invocation of void default Groovy static method")
  fun `invocation of void default Groovy static method`() {
    expect
    Thread.sleep(1)
    Thread.sleep(1)
  }

  @MigratedFrom("org.spockframework.smoke.VoidMethodCallsInExpectBlocks#invocation of statically imported void Groovy method")
  fun `invocation of statically imported void Groovy method`() {
    expect
    foo()
  }

  private fun voidInstanceMethod() {}

  companion object {
    @JvmStatic
    fun voidStaticMethod() {}
  }
}

object VoidKotlinStaticMethod {
  @JvmStatic
  fun foo() {}
}
