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

package io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace

import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.setup
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.variables
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import spock.lang.Specification
import java.io.IOException

class UnsatisfiedImplicitConditionSpec : Specification() {
  fun `a feature`() {
    expect
    false
  }
}

class UnsatisfiedExplicitConditionSpec : Specification() {
  fun `a feature`() {
    expect
    assert(false)
  }
}

class KotlinCallChainSpec : Specification() {
  fun `a feature`() {
    setup
    KotlinCallChain().a()

    expect
    true
  }
}

class JavaCallChainSpec : Specification() {
  fun `a feature`() {
    setup
    JavaCallChain().a()

    expect
    true
  }
}

class CallChainException : RuntimeException()

class ClosureInHelperMethodSpec : Specification() {
  fun `a feature`() {
    expect
    helper()
  }

  fun helper() {
    val cl = { assert(false) }
    cl()
  }
}

class ClosureInFixtureMethodSpec : Specification() {
  fun setup() {
    val cl = { assert(false) }
    cl()
  }

  fun `a feature`() {
    expect
    true
  }
}

class ClosureInFieldInitializerSpec : Specification() {
  val x = Runnable { assert(false) }.run()

  fun foo() {
    expect
    true
  }
}

class FailingSetupSpec : Specification() {
  fun setup() {
    assert(false)
  }

  fun feature() {
    expect
    true
  }
}

class FailingCleanupSpec : Specification() {
  fun cleanup() {
    assert(false)
  }

  fun feature() {
    expect
    true
  }
}

class FailingSetupSpecSpec : Specification() {
  fun setupSpec() {
    assert(false)
  }

  fun feature() {
    expect
    true
  }
}

class FailingCleanupSpecSpec : Specification() {
  fun cleanupSpec() {
    assert(false)
  }

  fun feature() {
    expect
    true
  }
}

abstract class FailingBaseSpec : Specification() {
  fun `let me fail`() {
    expect
    false
  }
}

class FailingDerivedSpec : FailingBaseSpec()

class FailingDataProviderCreationSpec : Specification() {
  fun foo(x: Int, y: Int, z: Int) {
    expect
    true

    where
    variable(x).from(listOf(1))
    variable(y).from(failingProvider())
    variable(z).from(listOf(1))
  }

  companion object {
    fun failingProvider(): List<Int> = throw RuntimeException()
  }
}

class FailingDataProcessorSpec : Specification() {
  fun foo(x: Int, y: Int, z: Int) {
    expect
    true

    where
    variable(x).from(listOf(1))
    variable(y).from(wronglyTypedProvider())
    variable(z).from(listOf(1))
  }

  companion object {
    @Suppress("UNCHECKED_CAST")
    fun wronglyTypedProvider() = listOf<Any>("not an int") as List<Int>

    @Suppress("UNCHECKED_CAST")
    fun <T> wronglyTypedValue() = "not an int" as T
  }
}

class FailingDerivedDataProcessorSpec : Specification() {
  fun foo(x: Int, y: Int, z: Int) {
    expect
    true

    where
    variable(x).from(listOf(1))
    variable(y).from(FailingDataProcessorSpec.wronglyTypedValue<Int>())
    variable(z).from(listOf(1))
  }
}

class FailingMultiVariableDataProcessorSpec : Specification() {
  fun foo(x: Int, y: Int, z: Int) {
    expect
    true

    where
    variables(x, y, z).from(listOf(1), FailingDataProcessorSpec.wronglyTypedProvider(), listOf(1))
  }
}

class FailingSetupBlockConditionSpec : Specification() {
  fun setup() {
    assert(1 > 2)
  }

  fun foo() {
    expect
    true
  }
}

class NestedExplicitConditionSpec : Specification() {
  fun `a feature`() {
    setup
    1.let { assert(1 > 2) }

    expect
    true
  }
}

class WrongExceptionCauseSpec : Specification() {
  fun `a feature`() {
    `when`
    throw IOException()

    then
    thrown(RuntimeException::class.java)
  }
}

class CauseLoopSpec : Specification() {
  fun `a feature`() {
    `when`
    val e = Exception()
    val cause = Exception(e)
    e.initCause(cause)
    throw e

    then
    thrown(RuntimeException::class.java)
  }
}

class SuppressedSpec : Specification() {
  fun `a feature`() {
    `when`
    val e = Exception()
    val suppressed = Exception()
    e.addSuppressed(suppressed)
    throw e

    then
    thrown(RuntimeException::class.java)
  }
}
