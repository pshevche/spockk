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

package io.github.pshevche.spockk.intellij

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration

class SpockkRunLineMarkerContributor : RunLineMarkerContributor() {

  override fun getInfo(element: PsiElement): Info? = calculateIcon(element)

  override fun getSlowInfo(element: PsiElement): Info? = calculateIcon(element)

  private fun calculateIcon(element: PsiElement): Info? {
    val parent = element.parent
    val actions = when {
      parent is KtClassOrObject && parent !is KtObjectDeclaration &&
        parent.nameIdentifier == element && parent.isSpockkSpec() ->
        ExecutorAction.getActions(0)

      parent is KtNamedFunction && parent.nameIdentifier == element &&
        parent.isSpockkFeature() ->
        ExecutorAction.getActions(1)

      else -> return null
    }
    return Info(AllIcons.RunConfigurations.TestState.Run, actions)
  }
}
