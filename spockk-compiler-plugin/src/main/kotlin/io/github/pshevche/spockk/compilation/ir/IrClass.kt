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

@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package io.github.pshevche.spockk.compilation.ir

import org.jetbrains.kotlin.fir.lazy.Fir2IrLazyPropertyForPureField
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.createDispatchReceiverParameterWithClassParent
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal fun IrClass.addMemberFunction(name: Name, returnType: IrType): IrFunction =
  addFunction {
    this.name = name
    this.returnType = returnType
  }
    .apply { parameters += createDispatchReceiverParameterWithClassParent() }

internal fun IrClassSymbol.findFieldByName(name: String): IrField =
  getBackingFields().single { it.name.asString() == name }

internal fun IrClassSymbol.findFieldByFqName(name: FqName): IrField =
  getBackingFields().single { it.symbol.owner.fqNameWhenAvailable == name }

private fun IrClassSymbol.getBackingFields(): List<IrField> = owner
  .declarations
  .filterIsInstance<Fir2IrLazyPropertyForPureField>()
  .map { it.backingField!! }
