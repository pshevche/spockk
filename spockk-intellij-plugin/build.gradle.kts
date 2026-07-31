import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
  alias(libs.plugins.intellij.platform)
  id("spockk.kotlin-library")
}

repositories { intellijPlatform { defaultRepositories() } }

dependencies {
  intellijPlatform {
    intellijIdea("2026.2")

    bundledPlugin("com.intellij.java")
    bundledPlugin("org.jetbrains.kotlin")
    bundledPlugin("org.jetbrains.plugins.gradle")

    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.Plugin.Java)
  }

  testImplementation(libs.hamcrest)
  testImplementation(libs.junit.jupiter)

  testRuntimeOnly(libs.junit4)
  testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
  useJUnitPlatform {
    excludeEngines("spock")
  }
}

val releaseNotesFile: RegularFile = layout.projectDirectory.dir("docs").file("release-notes.txt")

intellijPlatform {
  buildSearchableOptions = false
  pluginConfiguration {
    ideaVersion { sinceBuild = "262" }

    changeNotes = releaseNotesFile.asFile.readText()
  }
  publishing { token = System.getenv("ORG_GRADLE_PROJECT_jetbrainsMarketplaceToken") }
}
