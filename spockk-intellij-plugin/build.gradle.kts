import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
  alias(libs.plugins.intellij.platform)
  id("spockk.kotlin-library")
}

repositories { intellijPlatform { defaultRepositories() } }

dependencies {
  intellijPlatform {
    intellijIdeaCommunity("2025.2.4")

    bundledPlugin("org.jetbrains.kotlin")
    bundledPlugin("org.jetbrains.plugins.gradle")

    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.Plugin.Java)
  }

  testImplementation(libs.hamcrest)
  testImplementation(libs.junit4)
}

val releaseNotesFile = layout.projectDirectory.dir("docs").file("release-notes.txt")

intellijPlatform {
  buildSearchableOptions = false
  pluginConfiguration {
    ideaVersion { sinceBuild = "251" }

    changeNotes = releaseNotesFile.asFile.readText()
  }
  publishing { token = System.getenv("ORG_GRADLE_PROJECT_jetbrainsMarketplaceToken") }
}
