plugins {
  alias(libs.plugins.shadow)
  id("spockk.artifact-under-test-producer")
  id("spockk.kotlin-library")
  id("spockk.maven-central-publish")
}

tasks.shadowJar {
  archiveClassifier = ""
  mergeServiceFiles()
  include("io/github/pshevche/spockk/**")
  include("org/spockframework/**")
  include("spock/**")
  include("META-INF/spockk-core.kotlin_module")
  include("META-INF/services/org.junit.platform.engine.discovery.DiscoverySelectorIdentifierParser")
  include("META-INF/services/org.junit.platform.engine.TestEngine")
}

dependencies {
  api(libs.spock)
}

mavenPublishing {
  pom {
    name = "Spockk Framework Core Module"
    description = "Add-on for the Spock framework adding expressive BDD-style syntax for Kotlin."
  }
}
