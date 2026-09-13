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

package io.github.pshevche.spockk.compilation.transformer.interaction

import org.jetbrains.kotlin.ir.IrStatement

/**
 * The scope one `when`/`then` pair verifies its interactions in: [irEnter] and the [registrations]
 * open it ahead of the `when` block's own statements, [irLeave] closes it at the top of the paired
 * `then` block, where Spock checks the invocation counts.
 *
 * Having a scope is not the same as having something to register: a zero-argument
 * `noMoreInteractions()` declares no interaction to register but still opens a scope, and an
 * unbalanced `leaveScope()` would pop a scope nothing pushed. That is why the two sides of the pair
 * come from this one value rather than from a flag and a list that could disagree.
 */
internal class InteractionScope(
  private val mockController: FeatureMockController,
  val registrations: List<IrStatement>
) {

  fun irEnter(): IrStatement = mockController.irEnterScope()

  fun irLeave(): IrStatement = mockController.irLeaveScope()
}
