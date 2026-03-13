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

package io.github.pshevche.spockk.compilation.collector

import io.github.pshevche.spockk.compilation.common.SpockkTransformationContext.FixtureMethodKind
import io.github.pshevche.spockk.compilation.ir.assignableParameters
import org.jetbrains.kotlin.ir.declarations.IrFunction

internal object FixtureMethodIdentifiers {
  private val FIXTURE_METHOD_NAMES = mapOf(
    "setup" to FixtureMethodKind.SETUP,
    "cleanup" to FixtureMethodKind.CLEANUP,
    "setupSpec" to FixtureMethodKind.SETUP_SPEC,
    "cleanupSpec" to FixtureMethodKind.CLEANUP_SPEC
  )

  internal fun fixtureMethodKind(function: IrFunction): FixtureMethodKind? =
    if (function.assignableParameters().isEmpty()) fixtureMethodKind(function.name.asString()) else null

  internal fun fixtureMethodKind(functionName: String): FixtureMethodKind? =
    FIXTURE_METHOD_NAMES[functionName]
}
