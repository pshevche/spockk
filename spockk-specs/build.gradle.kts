plugins {
  `java-test-fixtures`
  id("spockk.artifact-under-test-consumer")
  id("spockk.compiler-plugin-consumer")
}

dependencies {
  testImplementation(projects.spockkCore)
  testImplementation(gradleTestKit())
  testImplementation(libs.junit.platform.testkit)
  testImplementation(libs.kotlin.compile.testing)
  testImplementation(libs.spock.junit4)
  testImplementation(libs.junit4)

  testRuntimeOnly(libs.junit.platform.launcher)
  testRuntimeOnly(libs.mockito)

  testFixturesImplementation(projects.spockkCompilerPlugin)
  testFixturesImplementation(projects.spockkCore)
  testFixturesImplementation(gradleTestKit())
  testFixturesImplementation(libs.junit.platform.testkit)
  testFixturesImplementation(libs.kotlin.compile.testing)
  testFixturesImplementation(libs.spock)
}

tasks.test {
  useJUnitPlatform()
  maxParallelForks = Runtime.getRuntime().availableProcessors() / 2

  systemProperty(
    "spockk.workspaceDir",
    layout.buildDirectory.dir("spockk-specs-workspaces").get().asFile.absolutePath
  )
  systemProperty("spockk.junitPlatformVersion", libs.versions.junit.get())

  jvmArgs(
    "-XX:+EnableDynamicAgentLoading" // To disable warning about byte-buddy-agent
  )
}
