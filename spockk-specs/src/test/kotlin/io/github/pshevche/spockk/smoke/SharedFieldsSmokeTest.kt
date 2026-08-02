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

import io.github.pshevche.spockk.lang.expect
import spock.lang.Shared
import spock.lang.Specification

/**
 * Base class with shared fields of different visibility modifiers.
 * Tests that shared fields are accessible from derived classes.
 */
abstract class SharedFieldsInSuperclassBase : Specification() {

  @Shared
  private val sharedPrivate = "sharedPrivate"

  @Shared
  protected val sharedProtected = "sharedProtected"

  @Shared
  val sharedPublic = "sharedPublic"

  @Shared
  internal val sharedProperty = "sharedProperty"

  fun `can access private shared field from base class`() {
    expect
    sharedPrivate == "sharedPrivate"
  }

  fun `can access protected shared field from base class`() {
    expect
    sharedProtected == "sharedProtected"
  }

  fun `can access public shared field from base class`() {
    expect
    sharedPublic == "sharedPublic"
  }

  fun `can access internal shared field from base class`() {
    expect
    sharedProperty == "sharedProperty"
  }
}

/**
 * Derived class that inherits shared fields from base class.
 * The features in the base class run in the context of this derived class.
 */
class SharedFieldsInSuperclass : SharedFieldsInSuperclassBase()
