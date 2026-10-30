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

import io.github.pshevche.spockk.lang.cleanup
import io.github.pshevche.spockk.lang.expect
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class AutoCleanupAfterCleanupSpec : Specification() {
  @AutoCleanup val closable = AutoCleanupResources.closable

  fun feature() {
    expect
    !closable.called

    cleanup
    assert(!closable.called)
  }

  fun cleanup() {
    assert(!closable.called)
  }
}

class AutoCleanupAfterCleanupSpecSpec : Specification() {
  @Shared @AutoCleanup
  val closable = AutoCleanupResources.closable

  fun feature() {
    expect
    !closable.called

    cleanup
    assert(!closable.called)
  }

  fun cleanup() {
    assert(!closable.called)
  }

  fun cleanupSpec() {
    assert(!closable.called)
  }
}

class AutoCleanupCustomMethodSpec : Specification() {
  @AutoCleanup("dispose")
  val disposable = AutoCleanupResources.disposable

  fun feature() {
    expect
    true
  }
}

class AutoCleanupErrorSpec : Specification() {
  @AutoCleanup val boom = Boom()

  fun feature() {
    expect
    true
  }
}

class AutoCleanupQuietSpec : Specification() {
  @AutoCleanup(quiet = true)
  val boom = Boom()

  fun feature() {
    expect
    true
  }
}

class AutoCleanupIndependentSpec : Specification() {
  @AutoCleanup val boom1 = AutoCleanupResources.boom1

  @AutoCleanup val boom2 = AutoCleanupResources.boom2

  fun feature() {
    expect
    true
  }
}

class AutoCleanupFieldInitFailsSpec : Specification() {
  @AutoCleanup val closable = AutoCleanupResources.closable
  val x = blowUp()

  fun feature() {
    expect
    true
  }
}

class AutoCleanupSharedFieldInitFailsSpec : Specification() {
  @Shared @AutoCleanup
  val closable = AutoCleanupResources.closable

  @Shared val x = blowUp()

  fun feature() {
    expect
    true
  }
}
