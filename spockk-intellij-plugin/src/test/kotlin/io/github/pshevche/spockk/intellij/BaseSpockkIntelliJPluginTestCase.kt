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

package io.github.pshevche.spockk.intellij

import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import java.nio.file.Paths

abstract class BaseSpockkIntelliJPluginTestCase : LightJavaCodeInsightFixtureTestCase() {

  override fun getTestDataPath(): String {
    val testCaseName = this::class.simpleName!!
    val path = Paths.get("./src/test/resources/$testCaseName/").toAbsolutePath()
    return path.toString()
  }

  protected fun CodeInsightTestFixture.configureFromDefaultFile(): PsiFile = this.configureByFile("/${this@BaseSpockkIntelliJPluginTestCase.name}.kt")

  protected fun <T : PsiElement> findRequiredElementByTextAndType(text: String, elementClass: Class<T>): T {
    val document = PsiDocumentManager.getInstance(project).getDocument(file)
    return document!!.text.allIndexesOf(text).firstNotNullOf {
      PsiTreeUtil.getParentOfType<T?>(file.findElementAt(it), elementClass)
    }
  }

  private fun String.allIndexesOf(sub: String): List<Int> {
    if (sub.isEmpty()) {
      return emptyList()
    }

    val result = mutableListOf<Int>()
    var index = indexOf(sub)

    while (index >= 0) {
      result += index
      index = indexOf(sub, startIndex = index + sub.length)
    }
    return result
  }
}
