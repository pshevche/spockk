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

import io.github.pshevche.spockk.compilation.collector.FixtureMethodIdentifiers.fixtureMethodKind
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.SPECIFICATION_FQN
import io.github.pshevche.spockk.compilation.shared.BaseSpockkIrElementVisitor
import io.github.pshevche.spockk.compilation.shared.FeatureBlock
import io.github.pshevche.spockk.compilation.shared.MutableSpockkTransformationContext
import io.github.pshevche.spockk.compilation.transformer.condition.containsImplicitAssertionHelperCall
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.types.isClassWithFqName
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.getAllSuperclasses
import org.jetbrains.kotlin.ir.util.isFakeOverride
import org.jetbrains.kotlin.ir.util.parentAsClass

internal class SpockkTransformationContextCollector(
  private val context: MutableSpockkTransformationContext
) : BaseSpockkIrElementVisitor() {

  override fun visitClassNew(declaration: IrClass) {
    if (isSpecification(declaration)) {
      context.addSpec(declaration)
    }
    super.visitClassNew(declaration)
  }

  private fun isSpecification(declaration: IrClass): Boolean =
    declaration.getAllSuperclasses().any {
      it.isClassWithFqName(SPECIFICATION_FQN)
    }

  override fun visitPropertyNew(declaration: IrProperty) {
    if (!declaration.isFakeOverride) {
      maybeCurrentIrClass?.let { context.addField(it, declaration) }
    }
    super.visitPropertyNew(declaration)
  }

  override fun visitFunctionNew(declaration: IrFunction) {
    if (declaration.isFakeOverride) {
      context.addPotentialFeature(currentIrClass, declaration)
    } else {
      fixtureMethodKind(declaration)?.let { kind ->
        maybeCurrentIrClass?.let { spec ->
          context.addFixtureMethod(spec, declaration)
        }
      }
    }
    super.visitFunctionNew(declaration)
  }

  override fun visitBlockBody(body: IrBlockBody) {
    val function = currentIrFunction
    if (fixtureMethodKind(function) != null) {
      FixtureMethodValidator(function.file, function, body).validate()
    } else {
      val bodyParser = createFeatureStatementsCollector(function.file)
      body.statements.forEach { bodyParser.consume(it) }
      val featureBody = bodyParser.getFeatureBody()
      if (featureBody != null) {
        validateFieldAccessInDataProviders(function.parentAsClass, featureBody.dataProviderBlocks)
        context.addFeature(currentIrClass, function, featureBody)
      } else if (function.parent == maybeCurrentIrClass && body.statements.containsImplicitAssertionHelperCall()) {
        context.addHelperMethodWithConditions(maybeCurrentIrClass!!, function)
      }
    }
    super.visitBlockBody(body)
  }

  private fun validateFieldAccessInDataProviders(spec: IrClass, dataProviderBlocks: List<FeatureBlock>) {
    dataProviderBlocks.forEach {
      InstanceFieldAccessChecker(spec, it.element.ir).check(it.statements)
    }
  }

  private fun createFeatureStatementsCollector(file: IrFile) =
    BlockOrderValidatingFeatureStatementsCollector(file, DefaultFeatureStatementsCollector(file))
}
