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
import org.jetbrains.kotlin.psi.KtNamedFunction

class SpockkRunLineMarkerContributor : RunLineMarkerContributor() {

  // Feature detection resolves the superclass hierarchy, which must not run on the EDT fast pass.
  // Return nothing here and let the slow pass compute the marker in the background.
  override fun getInfo(element: PsiElement): Info? = null

  // Only feature methods are marked. The spec-class gutter icon is already provided by the built-in
  // Spock plugin, so marking the class here too would show a duplicate icon.
  override fun getSlowInfo(element: PsiElement): Info? {
    val parent = element.parent
    if (parent is KtNamedFunction && parent.nameIdentifier == element && parent.isSpockkFeature()) {
      return Info(AllIcons.RunConfigurations.TestState.Run, ExecutorAction.getActions(1))
    }
    return null
  }
}
