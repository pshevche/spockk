plugins {
  id("com.gradle.develocity") version ("4.5.0")
}

develocity {
  projectId = "spockframework"
  server = "https://community.develocity.cloud"
  buildScan {
    publishing {
      onlyIf { it.isAuthenticated }
    }
  }
}

rootProject.name = "spockk"

includeBuild("gradle/plugins")

include("spockk-compiler-plugin")
include("spockk-core")
include("spockk-docs")
include("spockk-intellij-plugin")
include("spockk-gradle-plugin")
include("spockk-specs")
include("spockk-vscode-plugin")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
