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

package io.github.pshevche.spockk.compilation

import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@Suppress("unused")
class SpockkGradlePlugin : KotlinCompilerPluginSupportPlugin {
  override fun applyToCompilation(
    kotlinCompilation: KotlinCompilation<*>
  ): Provider<List<SubpluginOption>> {
    val project = kotlinCompilation.target.project
    return project.provider { listOf() }
  }

  override fun getCompilerPluginId(): String = SpockkCompilerPluginConfig.SPOCKK_COMPILER_PLUGIN_ID

  override fun getPluginArtifact(): SubpluginArtifact =
    SubpluginArtifact(
      SpockkCompilerPluginConfig.SPOCKK_COMPILER_PLUGIN_GROUP,
      SpockkCompilerPluginConfig.SPOCKK_COMPILER_ARTIFACT_ID,
      SpockkCompilerPluginConfig.SPOCKK_COMPILER_VERSION
    )

  override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true
}
