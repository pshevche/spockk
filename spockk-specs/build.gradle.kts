@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  `java-test-fixtures`
  alias(libs.plugins.kotlin.power.assert)
  id("spockk.artifact-under-test-consumer")
  id("spockk.compiler-plugin-consumer")
}

dependencies {
  testImplementation(projects.spockkCore)
  testImplementation(gradleTestKit())
  testImplementation(libs.spock)
  testImplementation(libs.junit.platform.testkit)
  testImplementation(libs.kotlin.compile.testing)

  testRuntimeOnly(libs.junit.platform.launcher)

  testFixturesImplementation(projects.spockkCompilerPlugin)
  testFixturesImplementation(projects.spockkCore)
  testFixturesImplementation(gradleTestKit())
  testFixturesImplementation(libs.junit.platform.testkit)
  testFixturesImplementation(libs.kotlin.compile.testing)
  testFixturesImplementation(libs.spock)
}

tasks.test {
  useJUnitPlatform()
  systemProperty(
    "spockk.workspaceDir",
    layout.buildDirectory.dir("spockk-specs-workspaces").get().asFile.absolutePath
  )
  systemProperty("spockk.junitPlatformVersion", libs.versions.junit.platform.get())
  systemProperty("spockk.kotlinVersion", libs.versions.kotlin.get())
  systemProperty("spockk.spockVersion", libs.versions.spock.get())
}

powerAssert {
  functions = setOf("kotlin.assert")
  includedSourceSets = setOf("test")
}
