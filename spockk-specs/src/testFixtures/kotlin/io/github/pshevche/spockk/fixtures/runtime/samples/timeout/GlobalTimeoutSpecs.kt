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

package io.github.pshevche.spockk.fixtures.runtime.samples.timeout

import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import spock.lang.Specification
import spock.lang.Timeout
import java.util.concurrent.TimeUnit

private const val FOREVER = 99999999999L

class SleepingFeatureSpec : Specification() {
  fun `a test`() {
    given
    Thread.sleep(FOREVER)

    expect
    true
  }
}

abstract class SleepingFeatureParentSpec : Specification() {
  fun `a test`() {
    given
    Thread.sleep(FOREVER)

    expect
    true
  }
}

class SleepingFeatureChildSpec : SleepingFeatureParentSpec() {
  fun `normal test`() {
    expect
    true
  }
}

class SleepingSetupSpecSpec : Specification() {
  fun setupSpec() {
    Thread.sleep(FOREVER)
  }

  fun `a test`() {
    expect
    true
  }
}

class SleepingSetupSpec : Specification() {
  fun setup() {
    Thread.sleep(FOREVER)
  }

  fun `a test`() {
    expect
    true
  }
}

class SleepingCleanupSpec : Specification() {
  fun cleanup() {
    Thread.sleep(FOREVER)
  }

  fun `a test`() {
    expect
    true
  }
}

class SleepingCleanupSpecSpec : Specification() {
  fun cleanupSpec() {
    Thread.sleep(FOREVER)
  }

  fun `a test`() {
    expect
    true
  }
}

abstract class SleepingSetupSpecParentSpec : Specification() {
  fun setupSpec() {
    Thread.sleep(FOREVER)
  }
}

abstract class SleepingSetupParentSpec : Specification() {
  fun setup() {
    Thread.sleep(FOREVER)
  }
}

abstract class SleepingCleanupParentSpec : Specification() {
  fun cleanup() {
    Thread.sleep(FOREVER)
  }
}

abstract class SleepingCleanupSpecParentSpec : Specification() {
  fun cleanupSpec() {
    Thread.sleep(FOREVER)
  }
}

class SleepingSetupSpecChildSpec : SleepingSetupSpecParentSpec() {
  fun `normal test`() {
    expect
    true
  }
}

class SleepingSetupChildSpec : SleepingSetupParentSpec() {
  fun `normal test`() {
    expect
    true
  }
}

class SleepingCleanupChildSpec : SleepingCleanupParentSpec() {
  fun `normal test`() {
    expect
    true
  }
}

class SleepingCleanupSpecChildSpec : SleepingCleanupSpecParentSpec() {
  fun `normal test`() {
    expect
    true
  }
}

class FeatureTimeoutOverrideSpec : Specification() {
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  fun `a test`() {
    given
    Thread.sleep(100)

    expect
    true
  }
}

@Timeout(value = 10, unit = TimeUnit.SECONDS)
class ClassTimeoutOverrideSpec : Specification() {
  fun setupSpec() {
    Thread.sleep(25)
  }

  fun setup() {
    Thread.sleep(25)
  }

  fun `a test`() {
    given
    Thread.sleep(25)

    expect
    true
  }

  fun cleanup() {
    Thread.sleep(25)
  }

  fun cleanupSpec() {
    Thread.sleep(25)
  }
}

class SlowFixturesSpec : Specification() {
  fun setupSpec() {
    Thread.sleep(25)
  }

  fun setup() {
    Thread.sleep(25)
  }

  fun `a test`() {
    expect
    true
  }

  fun cleanup() {
    Thread.sleep(25)
  }

  fun cleanupSpec() {
    Thread.sleep(25)
  }
}
