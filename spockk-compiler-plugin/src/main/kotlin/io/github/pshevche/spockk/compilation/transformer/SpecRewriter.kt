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

package io.github.pshevche.spockk.compilation.transformer

import io.github.pshevche.spockk.compilation.common.SpockkTransformationContext.SpecContext
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.SPEC_METADATA_FQN
import io.github.pshevche.spockk.compilation.ir.irAnnotation
import io.github.pshevche.spockk.compilation.transformer.fields.FieldsRewriter
import io.github.pshevche.spockk.compilation.transformer.mock.MockingApiTransformer
import io.github.pshevche.spockk.compilation.transformer.parametrization.WhereBlockRewriter
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall

internal class SpecRewriter(override val context: IrGeneratorContext) : SpockkIrRewriter {

  fun rewrite(spec: IrClass, context: SpecContext) {
    annotateSpec(spec, context)
    rewriteFields(spec, context)
    rewriteWhereBlocks(spec, context)
    rewriteMockingApi(spec)
  }

  private fun annotateSpec(spec: IrClass, context: SpecContext) {
    spec.annotations += specMetadataAnnotation(spec, context.fileName, context.line)
  }

  private fun rewriteFields(spec: IrClass, context: SpecContext) {
    // Always create FieldRewriter even when the spec has no fields of its own,
    // because registerParentSharedFields() needs to run for subclasses that
    // inherit shared fields from parent specs.
    FieldsRewriter(this.context, spec, context.fields).rewrite()
  }

  private fun rewriteWhereBlocks(spec: IrClass, context: SpecContext) {
    context.features.forEach { (feature, featureContext) ->
      val rewriter = WhereBlockRewriter(this.context, spec, feature, featureContext)
      featureContext.dataProviderBlocks.forEach {
        rewriter.rewrite(it)
      }
      rewriter.finalizeRewrite()
    }
  }

  private fun specMetadataAnnotation(
    spec: IrClass,
    fileName: String,
    line: Int
  ): IrConstructorCall =
    with(irBuilder(spec.symbol)) {
      irAnnotation(SPEC_METADATA_FQN, irString(fileName), irInt(line))
    }

  private fun rewriteMockingApi(spec: IrClass) {
    MockingApiTransformer(context, spec).rewriteMockingApi()
  }
}
