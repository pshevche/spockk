plugins {
  id("com.gradle.develocity") version ("4.4.3")
}

develocity {
  server = "https://ge.spockframework.org/"
  buildScan {
    publishing {
      onlyIf {
        it.isAuthenticated
      }
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

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
