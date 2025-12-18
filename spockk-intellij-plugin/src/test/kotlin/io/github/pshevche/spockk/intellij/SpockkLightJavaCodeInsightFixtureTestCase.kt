package io.github.pshevche.spockk.intellij;

import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

abstract class SpockkLightJavaCodeInsightFixtureTestCase : LightJavaCodeInsightFixtureTestCase() {

  fun <T : PsiElement> findRequiredElementByTextAndType(text: String, elementClass: Class<T>): T {
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
