plugins {
  id("spockk.artifact-under-test-producer")
  id("spockk.kotlin-library")
  id("spockk.maven-central-publish")
}

dependencies {
  compileOnly(libs.google.autoservice.annotations)
  implementation(libs.junit.platform.engine)
}

mavenPublishing {
  pom {
    name = "Spockk Framework Core Module"
    description = "Add-on for the Spock framework adding expressive BDD-style syntax for Kotlin."
  }
}
