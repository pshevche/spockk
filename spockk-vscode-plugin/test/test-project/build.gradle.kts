plugins {
  kotlin("jvm") version "2.4.10"
  id("io.github.pshevche.spockk") version "0.4.0"
}

repositories {
  mavenCentral()
  mavenLocal()
}

dependencies {
  testImplementation(kotlin("test"))
}
