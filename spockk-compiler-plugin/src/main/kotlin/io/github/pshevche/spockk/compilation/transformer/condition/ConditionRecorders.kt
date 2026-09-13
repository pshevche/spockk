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

package io.github.pshevche.spockk.compilation.transformer.condition

import org.jetbrains.kotlin.ir.declarations.IrVariable

/**
 * The two variables every rewritten condition reads: the `ValueRecorder` that captures its
 * intermediate values for the failure diagram, and the `ErrorCollector` its failure is reported to.
 *
 * Declared once per feature or helper method and shared by all of its conditions, so they travel
 * together. A method with no conditions declares neither, which is why callers hold this as a
 * nullable whole rather than two independently-nullable variables.
 */
internal class ConditionRecorders(
  val valueRecorder: IrVariable,
  val errorCollector: IrVariable
) {

  /** The same value recorder, reporting to a different collector - see `verifyAll`. */
  fun reportingTo(errorCollector: IrVariable) = ConditionRecorders(valueRecorder, errorCollector)
}
