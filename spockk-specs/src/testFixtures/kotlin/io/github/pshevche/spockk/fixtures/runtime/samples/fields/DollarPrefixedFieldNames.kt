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

package io.github.pshevche.spockk.fixtures.runtime.samples.fields

import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.where
import spock.lang.Shared
import spock.lang.Specification

class DollarPrefixedFieldNames : Specification() {
  var `$unshared` = 0

  @Shared var `$shared` = 0

  fun `feature`(count: Int) {
    expect
    assert(++`$unshared` == 1)
    assert(++`$shared` == count)

    where
    variable(count).from(1, 2, 3)
  }
}
