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

package io.github.pshevche.spockk.compilation.common

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.AND_BLOCK_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.CLEANUP_BLOCK_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.EXPECT_BLOCK_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.GIVEN_BLOCK_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.SETUP_BLOCK_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.THEN_BLOCK_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.WHEN_BLOCK_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.WHERE_BLOCK_FQN
import org.jetbrains.kotlin.name.FqName

internal enum class FeatureBlockLabel(
  val displayName: String,
  val fqn: FqName,
  val blockKind: String?
) {
  SETUP("setup", SETUP_BLOCK_FQN, null),
  GIVEN("given", GIVEN_BLOCK_FQN, null),
  WHEN("when", WHEN_BLOCK_FQN, "WHEN"),
  THEN("then", THEN_BLOCK_FQN, "THEN"),
  EXPECT("expect", EXPECT_BLOCK_FQN, "EXPECT"),
  AND("and", AND_BLOCK_FQN, null),
  WHERE("where", WHERE_BLOCK_FQN, "WHERE"),
  CLEANUP("cleanup", CLEANUP_BLOCK_FQN, "CLEANUP");

  companion object {
    fun from(fqn: FqName) = entries.find { fqn == it.fqn }
  }
}
