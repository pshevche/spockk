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
import spock.lang.Shared
import spock.lang.Specification

abstract class SharedFieldsInSuperclassBase : Specification() {

  @Shared
  private val sharedPrivate = "sharedPrivate"

  @Shared
  protected val sharedProtected = "sharedProtected"

  @Shared
  @JvmField
  val sharedPublic = "sharedPublic"

  @Shared
  val sharedProperty = "sharedProperty"

  @MigratedFrom("org.spockframework.smoke.SharedFieldsInSuperclass#can access shared private field")
  fun `can access shared private field`() {
    expect
    sharedPrivate == "sharedPrivate"
  }

  @MigratedFrom("org.spockframework.smoke.SharedFieldsInSuperclass#can access shared protected field")
  fun `can access shared protected field`() {
    expect
    sharedProtected == "sharedProtected"
  }

  @MigratedFrom("org.spockframework.smoke.SharedFieldsInSuperclass#can access shared public field")
  fun `can access shared public field`() {
    expect
    sharedPublic == "sharedPublic"
  }

  @MigratedFrom("org.spockframework.smoke.SharedFieldsInSuperclass#can access shared property")
  fun `can access shared property`() {
    expect
    sharedProperty == "sharedProperty"
  }
}

class SharedFieldsInSuperclass : SharedFieldsInSuperclassBase()
