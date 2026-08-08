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

package io.github.pshevche.spockk.compilation.transformer.condition

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.ERROR_COLLECTOR_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.ERROR_RETHROWER_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.VALUE_RECORDER_FQN
import io.github.pshevche.spockk.compilation.ir.findFieldByName
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
import io.github.pshevche.spockk.compilation.ir.irImplicitNotNull
import io.github.pshevche.spockk.compilation.ir.irType
import io.github.pshevche.spockk.compilation.ir.irVal
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers.ERROR_COLLECTOR_VAR
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers.VALUE_RECORDER_VAR
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers.VERIFY_ALL_ERROR_COLLECTOR_VAR
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.constructors

/**
 * A fresh `ValueRecorder` instance, declared once at the top of [enclosingFunction] (a feature
 * method or a helper method) and shared across every condition it contains.
 */
internal fun SpockkIrRewriter.irValueRecorderDeclaration(builder: DeclarationIrBuilder, enclosingFunction: IrFunction): IrVariable {
  val valueRecorderClass = rewriterContext.findRequiredClassSymbol(VALUE_RECORDER_FQN)
  val valueRecorderConstructor = valueRecorderClass.constructors.first()
  val newValueRecorderCall = builder.irCallConstructor(valueRecorderConstructor, listOf())

  return irVal(VALUE_RECORDER_VAR, builder.irType(VALUE_RECORDER_FQN)).apply {
    parent = enclosingFunction
    initializer = newValueRecorderCall
  }
}

/**
 * The stateless, fail-fast `ErrorRethrower.INSTANCE` singleton, declared once at the top of
 * [enclosingFunction] as the static error collector for conditions that should abort immediately
 * on the first failure (a feature's top-level conditions, `verify`/`verifyEach`, and a plain helper
 * method's own conditions). `ErrorRethrower`'s constructor is private, so `INSTANCE` is the only
 * value that can ever be referenced here - the same is true of Spock's own generated code.
 */
internal fun SpockkIrRewriter.irStaticErrorCollectorDeclaration(builder: DeclarationIrBuilder, enclosingFunction: IrFunction): IrVariable {
  val errorCollectorType = builder.irType(ERROR_COLLECTOR_FQN)
  val errorRethrowerType = builder.irType(ERROR_RETHROWER_FQN)
  val errorRethrowerClass = rewriterContext.findRequiredClassSymbol(ERROR_RETHROWER_FQN)
  val instanceField = errorRethrowerClass.findFieldByName("INSTANCE")

  val instanceFieldAccess = builder.irGetField(null, instanceField).apply {
    superQualifierSymbol = errorRethrowerClass
  }
  return irVal(ERROR_COLLECTOR_VAR, errorCollectorType).apply {
    parent = enclosingFunction
    initializer = builder.irImplicitNotNull(instanceFieldAccess, errorRethrowerType)
  }
}

/**
 * A fresh `ErrorCollector` instance, declared as the first statement of a `verifyAll` lambda body -
 * unlike the static [irStaticErrorCollectorDeclaration], this one has real per-scope state (the list
 * of collected failures), so a new instance is required for every `verifyAll` call.
 */
internal fun SpockkIrRewriter.irNewErrorCollectorDeclaration(builder: DeclarationIrBuilder, enclosingFunction: IrFunction): IrVariable {
  val errorCollectorClass = rewriterContext.findRequiredClassSymbol(ERROR_COLLECTOR_FQN)
  val errorCollectorConstructor = errorCollectorClass.constructors.first()
  val newErrorCollectorCall = builder.irCallConstructor(errorCollectorConstructor, listOf())

  return irVal(VERIFY_ALL_ERROR_COLLECTOR_VAR, builder.irType(ERROR_COLLECTOR_FQN)).apply {
    parent = enclosingFunction
    initializer = newErrorCollectorCall
  }
}
