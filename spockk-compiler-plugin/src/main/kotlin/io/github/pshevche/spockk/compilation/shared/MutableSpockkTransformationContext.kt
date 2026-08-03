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

package io.github.pshevche.spockk.compilation.shared

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers
import io.github.pshevche.spockk.compilation.ir.assignableParameters
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.interpreter.getLastOverridden
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.isClassWithFqName
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.fileEntry
import org.jetbrains.kotlin.ir.util.hasAnnotation
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

  fun hasSpecs() = specs.isNotEmpty()

  private fun computeSpecDepth(spec: IrClass): Int {
    val parentSpec = spec.superClass!!
    if (parentSpec.isClassWithFqName(IrIdentifiers.Spock.SPECIFICATION_FQN)) {
      return 0
    }

    return computeSpecDepth(parentSpec) + 1
  }

  fun addField(spec: IrClass, property: IrProperty) =
    specs[spec]?.addField(property)

  fun addFeature(
    spec: IrClass,
    feature: IrFunction,
    body: FeatureBody
  ) =
    specs[spec]?.addFeature(feature, body)

  fun addFixtureMethod(
    spec: IrClass,
    function: IrFunction
  ) = specs[spec]?.addFixtureMethod(function)

  fun addHelperMethodWithConditions(
    spec: IrClass,
    function: IrFunction
  ) = specs[spec]?.addHelperMethodWithConditions(function)

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
              finalizeFeatures(ctx),
              ctx.fields.toMap(),
              ctx.fixtureMethods.toList(),
              ctx.helperMethodsWithConditions.toSet()
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
    val features: MutableMap<IrFunction, SpockkTransformationContext.FeatureContext> =
      mutableMapOf()
    val potentialFeatures: MutableSet<IrFunction> = mutableSetOf()
    val fields: LinkedHashMap<IrProperty, SpockkTransformationContext.FieldContext> = linkedMapOf()
    val fixtureMethods: MutableSet<IrFunction> = mutableSetOf()
    val helperMethodsWithConditions: MutableSet<IrFunction> = mutableSetOf()

    fun addField(property: IrProperty) {
      val fileEntry = property.fileEntry
      val line = fileEntry.getLineNumber(property.startOffset) + 1
      val backingField = property.backingField
      val hasInitializer = backingField?.initializer != null
      val isShared = property.hasAnnotation(IrIdentifiers.Spock.SHARED_ANNOTATION_FQN) ||
        backingField?.hasAnnotation(IrIdentifiers.Spock.SHARED_ANNOTATION_FQN) ?: false
      fields[property] = SpockkTransformationContext.FieldContext(
        name = property.name.asString(),
        ordinal = fields.size,
        line = line,
        hasInitializer = hasInitializer,
        isShared = isShared,
        isVal = !property.isVar,
        isLateinit = property.isLateinit
      )
    }

    fun addFeature(
      feature: IrFunction,
      body: FeatureBody
    ) {
      features.computeIfAbsent(feature) {
        val file = feature.file
        val line = file.fileEntry.getLineNumber(feature.startOffset) + 1
        SpockkTransformationContext.FeatureContext(
          specDepth,
          featureOrdinal,
          feature.name.asString(),
          line,
          feature.assignableParameters().map { it.name.asString() },
          body
        )
          .also { featureOrdinal += 1 }
      }
    }

    fun addFixtureMethod(function: IrFunction) {
      fixtureMethods.add(function)
    }

    fun addHelperMethodWithConditions(function: IrFunction) {
      helperMethodsWithConditions.add(function)
    }

    fun addPotentialFeature(function: IrFunction) {
      potentialFeatures.add(function)
    }
  }
}
