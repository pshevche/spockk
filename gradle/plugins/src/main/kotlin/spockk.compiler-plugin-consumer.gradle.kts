import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("spockk.kotlin-library")
}

val compilerPlugin = configurations.create("compilerPlugin") {
  isCanBeResolved = true
  isCanBeConsumed = false
  attributes {
    attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "compiler-plugin"))
  }
}

dependencies {
  compilerPlugin(project(":spockk-compiler-plugin"))
}

kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xplugin=${compilerPlugin.singleFile.absolutePath}")
  }
}

tasks.withType<KotlinCompile>().configureEach {
  inputs.files(compilerPlugin)
}
