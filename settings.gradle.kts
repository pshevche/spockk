plugins {
  id("com.gradle.develocity") version ("4.2.2")
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
    termsOfUseAgree = "yes"
    publishing {
      onlyIf {
        it.buildResult.failures.isNotEmpty()
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
