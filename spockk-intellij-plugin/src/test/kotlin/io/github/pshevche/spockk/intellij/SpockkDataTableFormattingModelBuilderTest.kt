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

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager

class SpockkDataTableFormattingModelBuilderTest : BaseSpockkIntelliJPluginTestCase() {

  fun testSpockkTableBasicUsage() {
    // expect
    checkFormattingBeforeAndAfter()
  }

  fun testSpockkTableWithComments() {
    // expect
    checkFormattingBeforeAndAfter()
  }

  fun testSpockkTableWithFullwidthCharacters() {
    // expect
    checkFormattingBeforeAndAfter()
  }

  fun testSpockkTableWithLongTableParts() {
    // expect
    checkFormattingBeforeAndAfter()
  }

  fun testSpockkTableWithBlockDescription() {
    // expect
    checkFormattingBeforeAndAfter()
  }

  fun testSpockkTableWithMultipleParts() {
    // expect
    checkFormattingBeforeAndAfter()
  }

  private fun checkFormattingBeforeAndAfter() {
    myFixture.configureByFile("/$name/before.kt")

    WriteCommandAction.runWriteCommandAction(project) {
      CodeStyleManager.getInstance(project).reformat(myFixture.file)
    }

    myFixture.checkResultByFile("/$name/after.kt")
  }
}
