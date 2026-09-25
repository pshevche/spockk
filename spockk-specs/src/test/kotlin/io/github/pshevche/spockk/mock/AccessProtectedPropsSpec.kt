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

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import spock.lang.Specification

class AccessProtectedPropsSpec : Specification() {

  @MigratedFrom("org.spockframework.mock.AccessProtectedPropsSpec#Access protected const should be accessible in Groovy 3&4 Issue #1501")
  fun `Access protected const should be accessible in Groovy 3&4 Issue #1501`() {
    `when`
    val mySpy: AccessProtectedSubClass = Spy()

    then
    mySpy.accessStaticFlag()
  }

  @MigratedFrom("org.spockframework.mock.AccessProtectedPropsSpec#Access protected should be accessible in Groovy 3&4 Issue #1501")
  fun `Access protected should be accessible in Groovy 3&4 Issue #1501`() {
    `when`
    val mySpy: AccessProtectedSubClass = Spy()

    then
    mySpy.accessNonStaticFlag()
  }

  @MigratedFrom("org.spockframework.mock.AccessProtectedPropsSpec#Access protected fields via access methods without spy")
  fun `Access protected fields via access methods without spy`() {
    `when`
    val myNonSpy = AccessProtectedSubClass()

    then
    myNonSpy.accessNonStaticFlag()
    myNonSpy.accessStaticFlag()
  }
}

open class AccessProtectedBaseClass {
  companion object {
    @JvmStatic
    protected var staticFlag: Boolean = true
  }

  protected var nonStaticFlag: Boolean = true
}

class AccessProtectedSubClass : AccessProtectedBaseClass() {
  fun accessNonStaticFlag(): Boolean = nonStaticFlag

  fun accessStaticFlag(): Boolean = staticFlag
}
