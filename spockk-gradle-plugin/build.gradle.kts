plugins {
  `java-gradle-plugin`
  signing
  alias(libs.plugins.plugin.publish)
  id("spockk.artifact-under-test-producer")
  id("spockk.compiler-plugin-config")
  id("spockk.kotlin-library")
}

dependencies { implementation(kotlin("gradle-plugin-api")) }

gradlePlugin {
  val spockk by
    plugins.creating {
      id = "io.github.pshevche.spockk"
      displayName = "Spockk"
      description =
        "Applies the Spockk Kotlin compiler plugin to transform concise specification syntax into runnable tests."
      website = "https://pshevche.github.io/spockk"
      vcsUrl = "https://github.com/pshevche/spockk.git"
      tags = listOf("kotlin", "testing", "spockk", "compiler-plugin")
      implementationClass = "io.github.pshevche.spockk.compilation.SpockkGradlePlugin"
    }
}

val isCI = System.getenv("CI") != null

signing {
  isRequired = isCI
  useInMemoryPgpKeys(
    System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKeyId"),
    System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey"),
    System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKeyPassword")
  )
}
