plugins {
  `java-library`
  kotlin("jvm")
  id("io.gitlab.arturbosch.detekt")
  id("com.diffplug.spotless")
}

repositories {
  mavenCentral()
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

detekt {
  config.from(rootProject.layout.projectDirectory.file("gradle/config/detekt.yml"))
}

spotless {
  kotlin {
    // do not format kotlin files in resources
    target(
      "src/main/kotlin/**/*.kt",
      "src/test/kotlin/**/*.kt",
      "src/testFixtures/kotlin/**/*.kt"
    )
    ktfmt()
    ktlint()
    licenseHeaderFile(rootProject.layout.projectDirectory.file("gradle/config/licenseHeader.txt").asFile)
  }

  kotlinGradle {
    ktfmt()
    ktlint()
  }
}
