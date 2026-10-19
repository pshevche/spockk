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

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import org.spockframework.runtime.SpecUtil
import spock.lang.Ignore
import spock.lang.Specification

class SpecUtilSpec : Specification() {

  @MigratedFrom("org.spockframework.runtime.SpecUtilSpec#a regular class is not a spec")
  fun `a regular class is not a spec`() {
    expect
    !SpecUtil.isSpec(RegularClass::class.java)
    !SpecUtil.isRunnableSpec(RegularClass::class.java)
  }

  @MigratedFrom("org.spockframework.runtime.SpecUtilSpec#class Specification is not a spec")
  fun `class Specification is not a spec`() {
    expect
    !SpecUtil.isSpec(Specification::class.java)
    !SpecUtil.isRunnableSpec(Specification::class.java)
  }

  @MigratedFrom("org.spockframework.runtime.SpecUtilSpec#an abstract class extending Specification is a spec but isn't runnable")
  fun `an abstract class extending Specification is a spec but isn't runnable`() {
    expect
    SpecUtil.isSpec(AbstractSpec::class.java)
    !SpecUtil.isRunnableSpec(AbstractSpec::class.java)
  }

  @MigratedFrom("org.spockframework.runtime.SpecUtilSpec#a concrete class directly extending Specification is a spec and is runnable")
  fun `a concrete class directly extending Specification is a spec and is runnable`() {
    expect
    SpecUtil.isSpec(ConcreteSpec::class.java)
    SpecUtil.isRunnableSpec(ConcreteSpec::class.java)
  }

  @MigratedFrom("org.spockframework.runtime.SpecUtilSpec#a concrete class indirectly extending Specification is a spec and is runnable")
  fun `a concrete class indirectly extending Specification is a spec and is runnable`() {
    expect
    SpecUtil.isSpec(DerivedSpec::class.java)
    SpecUtil.isRunnableSpec(DerivedSpec::class.java)
  }

  class RegularClass : ArrayList<Any>()
}

abstract class AbstractSpec : Specification()

@Ignore
open class ConcreteSpec : Specification()

@Ignore
class DerivedSpec : ConcreteSpec()
