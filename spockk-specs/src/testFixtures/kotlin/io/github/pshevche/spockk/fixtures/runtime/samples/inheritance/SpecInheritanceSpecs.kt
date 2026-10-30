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

package io.github.pshevche.spockk.fixtures.runtime.samples.inheritance

import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.setup
import spock.lang.Ignore
import spock.lang.Specification
import java.io.IOException

object InheritanceTracker {
  val featureOrder = mutableListOf<Int>()
  val overriddenFeature = mutableListOf<Int>()
}

open class FeatureOrderBase : Specification() {
  fun `1`() {
    InheritanceTracker.featureOrder.add(1)
    expect
    true
  }

  fun `2`() {
    InheritanceTracker.featureOrder.add(2)
    expect
    true
  }
}

class FeatureOrderDerived : FeatureOrderBase() {
  fun `3`() {
    InheritanceTracker.featureOrder.add(3)
    expect
    true
  }

  fun `4`() {
    InheritanceTracker.featureOrder.add(4)
    expect
    true
  }
}

open class BaseSetupThrows : Specification() {
  open fun setup(): Unit = throw IOException()
}

class DerivedSetupThrows : BaseSetupThrows() {
  override fun setup() {}

  fun feature() {
    expect
    true
  }
}

open class BaseCleanupThrows : Specification() {
  open fun cleanup(): Unit = throw IOException()
}

class DerivedCleanupThrows : BaseCleanupThrows() {
  override fun cleanup() {}

  fun feature() {
    expect
    true
  }
}

open class BaseSetupSpecThrows : Specification() {
  open fun setupSpec(): Unit = throw IOException()
}

class DerivedSetupSpecThrows : BaseSetupSpecThrows() {
  override fun setupSpec() {}

  fun feature() {
    expect
    true
  }
}

open class BaseCleanupSpecThrows : Specification() {
  open fun cleanupSpec(): Unit = throw IOException()
}

class DerivedCleanupSpecThrows : BaseCleanupSpecThrows() {
  override fun cleanupSpec() {}

  fun feature() {
    expect
    true
  }
}

open class CountsBase : Specification() {
  fun f1() {
    expect
    true
  }

  fun f2() {
    expect
    assert(false)
  }

  @Ignore
  fun f3() {
    expect
    assert(false)
  }
}

class CountsDerived : CountsBase() {
  fun f4() {
    expect
    assert(false)
  }

  fun f5() {
    expect
    true
  }
}

open class OverriddenFeatureBase : Specification() {
  open fun foo() {
    setup
    InheritanceTracker.overriddenFeature.add(1)
    expect
    true
  }
}

class OverriddenFeatureDerived : OverriddenFeatureBase() {
  override fun foo() {
    setup
    InheritanceTracker.overriddenFeature.add(2)
    expect
    true
  }
}

open class SuperHelperBase : Specification() {
  open fun helperMethod(): String = "helper invoked"
}

class SuperHelperDerived : SuperHelperBase() {
  override fun helperMethod(): String = super.helperMethod()

  fun feature() {
    expect
    helperMethod() == "helper invoked"
  }
}
