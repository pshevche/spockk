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
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * Regression guard: `@JvmField @Rule` puts the annotation directly on the field and must keep
 * working unchanged.
 */
class JvmFieldRuleSmokeTest : Specification() {

  @JvmField
  @Rule
  val tmp = TemporaryFolder()

  fun `rule created via JvmField still runs`() {
    expect
    tmp.root.isDirectory
  }
}
