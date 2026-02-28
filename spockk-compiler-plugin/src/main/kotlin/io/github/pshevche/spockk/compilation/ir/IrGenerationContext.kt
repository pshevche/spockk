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

@file:OptIn(InternalSymbolFinderAPI::class, UnsafeDuringIrConstructionAPI::class)

package io.github.pshevche.spockk.compilation.ir

import org.jetbrains.kotlin.backend.common.CompilationException
import org.jetbrains.kotlin.ir.InternalSymbolFinderAPI
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

internal fun IrGeneratorContext.findRequiredClassSymbol(classFqn: FqName): IrClassSymbol =
  irBuiltIns.symbolFinder.findClass(ClassId.topLevel(classFqn))
    ?: throw CompilationException("Cannot find class ${classFqn.asString()}", null, null, null)

internal fun IrGeneratorContext.findPropertyGetter(callableId: CallableId): IrFunction =
  irBuiltIns.symbolFinder.findProperties(callableId).first().owner.getter!!

internal fun IrGeneratorContext.findUniqueFunctionSymbol(callableId: CallableId): IrFunctionSymbol =
  findFunctionSymbols(callableId).first()

internal fun IrGeneratorContext.findFunctionSymbols(
  callableId: CallableId
): Iterable<IrFunctionSymbol> = irBuiltIns.symbolFinder.findFunctions(callableId)
