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

package io.github.pshevche.spockk.compilation.ir

import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.declarations.IrProperty

internal fun IrProperty.makePrivate() {
  visibility = DescriptorVisibilities.PRIVATE
  getter?.visibility = DescriptorVisibilities.PRIVATE
  setter?.visibility = DescriptorVisibilities.PRIVATE
}

internal fun IrProperty.makeProtected() {
  // Backing fields are always private in Kotlin — only property/accessor visibility changes
  visibility = DescriptorVisibilities.PROTECTED
  getter?.visibility = DescriptorVisibilities.PROTECTED
  setter?.visibility = DescriptorVisibilities.PROTECTED
}

internal fun IrProperty.makeMutable() {
  isVar = true
}
