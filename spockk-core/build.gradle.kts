plugins {
  alias(libs.plugins.shadow)
  id("spockk.artifact-under-test-producer")
  id("spockk.kotlin-library")
  id("spockk.maven-central-publish")
}

// Kotlin 2.4.0 changed the naming of the kotlin_module file, making it incompatible with Kotlin 2.3.x and older
// This task creates a kotlin_module file with the same content under the old name to keep the compiler plugin compatible with older Kotlin versions
val generateLegacyKotlinModuleMetadata = tasks.register<Copy>("generateLegacyKotlinModuleMetadata") {
  dependsOn(tasks.compileKotlin)
  from(layout.buildDirectory.dir("classes/kotlin/main/META-INF")) {
    include("io.github.pshevche.spockk_spockk-core.kotlin_module")
    rename("io.github.pshevche.spockk_spockk-core.kotlin_module", "spockk-core.kotlin_module")
  }
  into(layout.buildDirectory.dir("classes/kotlin/main/META-INF"))
}

tasks.jar {
  dependsOn(generateLegacyKotlinModuleMetadata)
}

tasks.shadowJar {
  dependsOn(generateLegacyKotlinModuleMetadata)
  archiveClassifier = ""
  mergeServiceFiles()
  include("io/github/pshevche/spockk/**")
  include("org/spockframework/**")
  include("spock/**")
  include("META-INF/io.github.pshevche.spockk_spockk-core.kotlin_module")
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
