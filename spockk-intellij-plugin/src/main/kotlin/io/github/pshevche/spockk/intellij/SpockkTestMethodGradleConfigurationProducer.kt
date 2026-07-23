package io.github.pshevche.spockk.intellij

import com.intellij.openapi.module.Module
import org.jetbrains.kotlin.idea.gradleJava.run.AbstractKotlinTestMethodGradleConfigurationProducer

class SpockkTestMethodGradleConfigurationProducer :
  AbstractKotlinTestMethodGradleConfigurationProducer() {

  override val forceGradleRunner: Boolean get() = false

  override val hasTestFramework: Boolean get() = true

  override fun isApplicable(module: Module): Boolean = true
}
