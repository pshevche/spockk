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

package io.github.pshevche.spockk.compilation.transformer.parametrization

import org.jetbrains.kotlin.backend.common.CompilationException
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile

internal class InvalidParametrizationExceptionFactory(
  private val file: IrFile,
  private val whereBlockIr: IrElement
) {

  fun unrecognizedParameterizationSyntaxException() =
    CompilationException(
      """
          where-blocks may only contain parametrization, e.g.
             a ; b
             1 ; 2
             2 ; 3
          """
        .trimIndent(),
      file,
      whereBlockIr
    )

  fun invalidRowSizeException(rowIdx: Int, actualSize: Int, expectedSize: Int) =
    CompilationException(
      "Row #$rowIdx in the data table has a wrong number of elements ($actualSize instead of $expectedSize)",
      file,
      whereBlockIr
    )

  fun missingValuesRowsException() =
    CompilationException("Data table must have more than just the header row", file, whereBlockIr)

  fun invalidDataTableHeaderException() =
    CompilationException(
      "Header of data table may only contain variable names",
      file,
      whereBlockIr
    )

  fun invalidDataPipeTargetException() =
    CompilationException(
      "Data pipe target must be declared inline as 'variable(a)' or 'variables(a, b)'",
      file,
      whereBlockIr
    )

  fun invalidFeatureVariableReferenceException() =
    CompilationException(
      "Data provider must reference a feature method variable",
      file,
      whereBlockIr
    )

  fun invalidPreviousVariableReferenceInDataPipeException() =
    CompilationException(
      "Data pipes may reference other feature variables only if the reference is the only value (valid: 'variable(a).from(other + 1)'; invalid: 'variable(a).from(1, other)')",
      file,
      whereBlockIr
    )

  fun invalidDataPipeSyntaxException() =
    CompilationException(
      "Data pipes must follow the 'variable(a).from(1, 2)' or 'variables(a, b).from(listOf(1, 2), listOf(2, 3))' syntax",
      file,
      whereBlockIr
    )
}
