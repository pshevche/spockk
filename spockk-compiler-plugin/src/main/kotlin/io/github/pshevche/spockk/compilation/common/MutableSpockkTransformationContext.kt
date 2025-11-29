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

package io.github.pshevche.spockk.compilation.common

import io.github.pshevche.spockk.compilation.ir.isThis
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.interpreter.getLastOverridden
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.isClassWithFqName
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.superClass

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class MutableSpockkTransformationContext {
  private val specs: MutableMap<IrClass, MutableSpecContext> = mutableMapOf()

  fun addSpec(spec: IrClass) {
    val specDepth = computeSpecDepth(spec)
    specs.computeIfAbsent(spec) {
      val file = spec.file
      val specLine = file.fileEntry.getLineNumber(spec.startOffset) + 1
      MutableSpecContext(file.name, specLine, specDepth)
    }
  }

  private fun computeSpecDepth(spec: IrClass): Int {
    val parentSpec = spec.superClass!!
    if (parentSpec.isClassWithFqName(SpockkConstants.SPECIFICATION_FQN)) {
      return 0
    }

    return computeSpecDepth(parentSpec) + 1
  }

  fun addFeature(spec: IrClass, feature: IrFunction, blocks: List<FeatureBlockStatements>) =
    specs[spec]?.addFeature(feature, blocks)

  fun addPotentialFeature(spec: IrClass, function: IrFunction) =
    specs[spec]?.addPotentialFeature(function)

  fun finalized(): SpockkTransformationContext =
    SpockkTransformationContext(
      buildMap {
        specs.forEach { (spec, ctx) ->
          put(
            spec,
            SpockkTransformationContext.SpecContext(
              ctx.fileName,
              ctx.line,
              finalizeFeatures(ctx)
            )
          )
        }
      }
    )

  private fun finalizeFeatures(
    ctx: MutableSpecContext
  ): Map<IrFunction, SpockkTransformationContext.FeatureContext> = buildMap {
    putAll(determineInheritedFeatures(ctx))
    putAll(ctx.features)
  }

  private fun determineInheritedFeatures(
    ctx: MutableSpecContext
  ): Map<IrFunction, SpockkTransformationContext.FeatureContext> =
    ctx.potentialFeatures
      .mapNotNull { func -> inheritedContext(func)?.let { func to it } }
      .toMap()

  private fun inheritedContext(function: IrFunction): SpockkTransformationContext.FeatureContext? {
    val overriddenFunc = function.getLastOverridden()
    return overriddenFunc.parentClassOrNull?.let { specs[it]?.features[overriddenFunc] }
  }

  internal class MutableSpecContext(val fileName: String, val line: Int, val specDepth: Int) {
    var featureOrdinal: Int = 0
    var features: MutableMap<IrFunction, SpockkTransformationContext.FeatureContext> =
      mutableMapOf()
    var potentialFeatures: MutableSet<IrFunction> = mutableSetOf()

    fun addFeature(feature: IrFunction, blocks: List<FeatureBlockStatements>) {
      features.computeIfAbsent(feature) {
        val file = feature.file
        val line = file.fileEntry.getLineNumber(feature.startOffset) + 1
        SpockkTransformationContext.FeatureContext(
          specDepth,
          featureOrdinal,
          feature.name.asString(),
          line,
          feature.parameters.filter { !it.isThis() }.map { it.name.asString() },
          blocks
        )
          .also { featureOrdinal += 1 }
      }
    }

    fun addPotentialFeature(function: IrFunction) {
      potentialFeatures.add(function)
    }
  }
}
