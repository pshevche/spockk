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

package io.github.pshevche.spockk.fixtures.runtime.samples.fixture

import io.github.pshevche.spockk.lang.expect
import spock.lang.Specification

object FixtureMethodsOrderLog {
  val log = mutableListOf<String>()
}

class FixtureMethodsOrderSpec : Specification() {
  fun setup() {
    FixtureMethodsOrderLog.log.add("s")
  }

  fun cleanup() {
    FixtureMethodsOrderLog.log.add("c")
  }

  fun setupSpec() {
    FixtureMethodsOrderLog.log.add("ss")
  }

  fun cleanupSpec() {
    FixtureMethodsOrderLog.log.add("cs")
  }

  fun feature() {
    expect
    true
  }
}

object FixtureMethodsHierarchyLog {
  val log = mutableListOf<String>()
}

abstract class FixtureMethodsHierarchyBase : Specification() {
  open fun setup() {
    FixtureMethodsHierarchyLog.log.add("s1")
  }

  open fun cleanup() {
    FixtureMethodsHierarchyLog.log.add("c1")
  }

  open fun setupSpec() {
    FixtureMethodsHierarchyLog.log.add("ss1")
  }

  open fun cleanupSpec() {
    FixtureMethodsHierarchyLog.log.add("cs1")
  }
}

class FixtureMethodsHierarchyDerived : FixtureMethodsHierarchyBase() {
  override fun setup() {
    FixtureMethodsHierarchyLog.log.add("s2")
  }

  override fun cleanup() {
    FixtureMethodsHierarchyLog.log.add("c2")
  }

  override fun setupSpec() {
    FixtureMethodsHierarchyLog.log.add("ss2")
  }

  override fun cleanupSpec() {
    FixtureMethodsHierarchyLog.log.add("cs2")
  }

  fun feature() {
    expect
    true
  }
}

object FixtureMethodsFailingSetupLog {
  val log = mutableListOf<String>()
}

class FixtureMethodsFailingSetupSpec : Specification() {
  fun setup(): Unit = throw RuntimeException()

  fun feature() {
    expect
    true
  }

  fun cleanup() {
    FixtureMethodsFailingSetupLog.log.add("cleanup")
  }
}

object FixtureMethodsFailingInitializerLog {
  val log = mutableListOf<String>()
}

class FixtureMethodsBlowUp {
  constructor() {
    throw RuntimeException()
  }
}

class FixtureMethodsFailingInitializerSpec : Specification() {
  val x = FixtureMethodsBlowUp()

  fun feature() {
    expect
    true
  }

  fun cleanup() {
    FixtureMethodsFailingInitializerLog.log.add("cleanup")
  }
}
