plugins {
  checkstyle
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

checkstyle {
  configFile = rootProject.layout.projectDirectory.file("gradle/config/checkstyle.xml").asFile
  maxWarnings = 3
}

detekt {
  config.from(rootProject.layout.projectDirectory.file("gradle/config/detekt.yml"))
}

spotless {
  kotlin {
    target("src/main/kotlin/**/*.kt", "src/test/kotlin/**/*.kt")
    ktfmt()
    ktlint()
    licenseHeaderFile(rootProject.layout.projectDirectory.file("gradle/config/licenseHeader.txt").asFile)
  }

  kotlinGradle {
    ktfmt()
    ktlint()
  }
}
