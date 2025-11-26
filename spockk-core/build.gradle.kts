plugins {
  alias(libs.plugins.kotlin.kapt)
  id("spockk.artifact-under-test-producer")
  id("spockk.kotlin-library")
  id("spockk.maven-central-publish")
}

dependencies {
  kapt(libs.google.autoservice)

  compileOnly(libs.google.autoservice.annotations)
  implementation(libs.junit.platform.engine)
}

mavenPublishing {
  pom {
    name = "Spockk Framework Core Module"
    description =
      "Kotlin-native testing and specification framework with expressive BDD-style syntax."
  }
}
