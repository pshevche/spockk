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

package io.github.pshevche.spockk.fixtures.runtime.samples.extension

import io.github.pshevche.spockk.lang.expect
import org.spockframework.runtime.model.parallel.ExecutionMode
import spock.lang.Ignore
import spock.lang.ResourceLock
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
class StepwiseFailingSpec : Specification() {
  fun step1() {
    expect
    true
  }

  fun step2() {
    expect
    false
  }

  fun step3() {
    expect
    true
  }

  fun step4() {
    expect
    true
  }
}

@Stepwise
class StepwiseResourceLockSpec : Specification() {
  fun step1() {
    expect
    true
  }

  @ResourceLock("a")
  fun step2() {
    expect
    true
  }

  fun step3() {
    expect
    true
  }

  fun step4() {
    expect
    true
  }
}

@Stepwise
class StepwiseIgnoreSpec : Specification() {
  fun step1() {
    expect
    true
  }

  @Ignore
  fun step2() {
    expect
    true
  }

  fun step3() {
    expect
    true
  }
}

@Stepwise
class StepwiseExecutionModeSpec : Specification() {
  fun step1() {
    expect
    specificationContext.currentSpec.childExecutionMode.get() == ExecutionMode.SAME_THREAD
  }
}
