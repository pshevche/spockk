import org.gradle.api.tasks.PathSensitivity

plugins {
  base
}

val isWindows = System.getProperty("os.name").lowercase().contains("windows")
val pnpmCommand = if (isWindows) "pnpm.cmd" else "pnpm"

val docsDir = layout.projectDirectory.dir("docs")
val nodeModulesDir = layout.projectDirectory.dir("node_modules")
val vitepressCacheDir = docsDir.dir(".vitepress/cache")

val pnpmInstall = tasks.register<Exec>("pnpmInstall") {
  inputs.file(layout.projectDirectory.file("package.json"))
  inputs.file(layout.projectDirectory.file("pnpm-lock.yaml"))
  outputs.dir(nodeModulesDir)

  workingDir(layout.projectDirectory)
  commandLine(pnpmCommand, "install", "--frozen-lockfile")
}

val pnpmBuild = tasks.register<Exec>("pnpmBuild") {
  dependsOn(pnpmInstall)

  val spockkVersion = project.property("version").toString()
  val junitPlatformVersion = libs.versions.junit.get()

  inputs.files(
    fileTree(docsDir) {
      exclude(".vitepress/cache", ".vitepress/dist")
    }
  ).withPropertyName("docsSource").withPathSensitivity(PathSensitivity.RELATIVE)
  inputs.dir(nodeModulesDir).withPropertyName("nodeModules").withPathSensitivity(PathSensitivity.RELATIVE)
  inputs.property("spockkVersion", spockkVersion)
  inputs.property("junitPlatformVersion", junitPlatformVersion)
  outputs.dir(layout.buildDirectory.dir("docs"))

  workingDir(layout.projectDirectory)
  environment(
    mapOf(
      "SPOCKK_VERSION" to spockkVersion,
      "JUNIT_PLATFORM_VERSION" to junitPlatformVersion
    )
  )
  commandLine(pnpmCommand, "run", "build")
}

tasks.named("build") {
  dependsOn(pnpmBuild)
}

tasks.named<Delete>("clean") {
  delete(vitepressCacheDir)
}
