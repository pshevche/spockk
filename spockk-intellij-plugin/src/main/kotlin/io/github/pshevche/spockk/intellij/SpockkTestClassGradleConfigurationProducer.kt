package io.github.pshevche.spockk.intellij

import com.intellij.openapi.module.Module
import org.jetbrains.kotlin.idea.gradleJava.run.AbstractKotlinTestClassGradleConfigurationProducer
import org.jetbrains.kotlin.idea.gradleJava.run.KotlinGradleConfigurationProducer

class SpockkTestClassGradleConfigurationProducer :
  AbstractKotlinTestClassGradleConfigurationProducer(),
  KotlinGradleConfigurationProducer {

  override fun getForceGradleRunner(): Boolean = false

  override fun getHasTestFramework(): Boolean = true

  override fun isApplicable(module: Module): Boolean = true
}
