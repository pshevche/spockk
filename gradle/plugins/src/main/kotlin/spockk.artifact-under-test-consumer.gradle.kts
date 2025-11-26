plugins {
  id("spockk.kotlin-library")
}

val artifactUnderTest by configurations.creating {
  isCanBeResolved = true
  isCanBeConsumed = false
  attributes {
    attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "artifact-under-test"))
  }
}

val gradlePluginProject = project(":spockk-gradle-plugin")
val compilerPluginProject = project(":spockk-compiler-plugin")
val coreProject = project(":spockk-core")
dependencies {
  artifactUnderTest(gradlePluginProject)
  artifactUnderTest(compilerPluginProject)
  artifactUnderTest(coreProject)
}

tasks.withType<Test>().configureEach {
  dependsOn(
    gradlePluginProject.tasks.named("publishAllPublicationsToRepoForTestRepository"),
    compilerPluginProject.tasks.named("publishAllPublicationsToRepoForTestRepository"),
    coreProject.tasks.named("publishAllPublicationsToRepoForTestRepository")
  )
  inputs.files(artifactUnderTest)
  systemProperty("spockk.gradlePluginRepo", gradlePluginProject.extra["repoForTest"].toString())
  systemProperty("spockk.compilerPluginRepo", compilerPluginProject.extra["repoForTest"].toString())
  systemProperty("spockk.coreRepo", coreProject.extra["repoForTest"].toString())
}
