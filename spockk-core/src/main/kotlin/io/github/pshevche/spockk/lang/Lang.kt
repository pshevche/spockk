/*
 * Copyright 2025 the original author or authors.
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

@file:Suppress("unused", "ClassName", "FunctionNaming")

package io.github.pshevche.spockk.lang

private fun throwIllegalLabelUsageException(label: String): Unit =
  throw UnsupportedOperationException(
    "The 'given' label should not be used outside of Spockk feature methods"
  )

object given {
  init {
    throwIllegalLabelUsageException("given")
  }
}

fun given(description: String) {
  throwIllegalLabelUsageException("given")
}

object expect {
  init {
    throwIllegalLabelUsageException("expect")
  }
}

fun expect(description: String) {
  throwIllegalLabelUsageException("expect")
}

object `when` {
  init {
    throwIllegalLabelUsageException("when")
  }
}

fun `when`(description: String) {
  throwIllegalLabelUsageException("when")
}

object then {
  init {
    throwIllegalLabelUsageException("then")
  }
}

fun then(description: String) {
  throwIllegalLabelUsageException("then")
}

object and {
  init {
    throwIllegalLabelUsageException("and")
  }
}

fun and(description: String) {
  throwIllegalLabelUsageException("and")
}
