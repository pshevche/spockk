import com.github.gradle.node.pnpm.task.PnpmTask
import org.gradle.api.tasks.PathSensitivity

plugins {
  base
  alias(libs.plugins.node)
}

val docsDir = layout.projectDirectory.dir("docs")
val nodeModulesDir = layout.projectDirectory.dir("node_modules")
val vitepressCacheDir = docsDir.dir(".vitepress/cache")

// Single source of truth for the pnpm version: package.json's "packageManager" field.
val pnpmVersionFromPackageJson =
  Regex(""""packageManager"\s*:\s*"pnpm@([^"]+)"""")
    .find(layout.projectDirectory.file("package.json").asFile.readText())
    ?.groupValues?.get(1)
    ?: error("Could not find a \"packageManager\": \"pnpm@...\" field in package.json")

node {
  version.set("24.19.0")
  pnpmVersion.set(pnpmVersionFromPackageJson)
  download.set(true)
}

// The node plugin already registers a "pnpmInstall" task (with pnpmCommand
// defaulted to "install"); configure it instead of registering a second
// task under the same name, adding only the extra install flag via args.
val pnpmInstall = tasks.named<PnpmTask>("pnpmInstall") {
  args.set(listOf("--frozen-lockfile"))

  inputs.file(layout.projectDirectory.file("package.json"))
  inputs.file(layout.projectDirectory.file("pnpm-lock.yaml"))
  outputs.dir(nodeModulesDir)
}

val pnpmBuild = tasks.register<PnpmTask>("pnpmBuild") {
  dependsOn(pnpmInstall)
  args.set(listOf("run", "build"))
  environment.set(
    mapOf(
      "SPOCKK_VERSION" to project.property("version").toString(),
      "JUNIT_PLATFORM_VERSION" to libs.versions.junit.get()
    )
  )

  inputs.files(
    fileTree(docsDir) {
      exclude(".vitepress/cache", ".vitepress/dist")
    }
  ).withPropertyName("docsSource").withPathSensitivity(PathSensitivity.RELATIVE)
  inputs.dir(nodeModulesDir).withPropertyName("nodeModules").withPathSensitivity(PathSensitivity.RELATIVE)
  outputs.dir(layout.buildDirectory.dir("docs"))
}

val pnpmLint = tasks.register<PnpmTask>("pnpmLint") {
  dependsOn(pnpmInstall)
  args.set(listOf("run", "lint"))

  inputs.files(
    fileTree(docsDir) {
      exclude(".vitepress/cache", ".vitepress/dist")
    }
  ).withPropertyName("docsSource").withPathSensitivity(PathSensitivity.RELATIVE)
  inputs.dir(nodeModulesDir).withPropertyName("nodeModules").withPathSensitivity(PathSensitivity.RELATIVE)
  outputs.file(layout.buildDirectory.file("pnpmLint.marker"))

  doLast {
    outputs.files.singleFile.writeText("ok")
  }
}

val pnpmFormatCheck = tasks.register<PnpmTask>("pnpmFormatCheck") {
  dependsOn(pnpmInstall)
  args.set(listOf("run", "format:check"))

  inputs.files(
    fileTree(docsDir) {
      exclude(".vitepress/cache", ".vitepress/dist")
    }
  ).withPropertyName("docsSource").withPathSensitivity(PathSensitivity.RELATIVE)
  inputs.dir(nodeModulesDir).withPropertyName("nodeModules").withPathSensitivity(PathSensitivity.RELATIVE)
  outputs.file(layout.buildDirectory.file("pnpmFormatCheck.marker"))

  doLast {
    outputs.files.singleFile.writeText("ok")
  }
}

tasks.named("check") {
  dependsOn(pnpmLint, pnpmFormatCheck)
}

tasks.named("build") {
  dependsOn(pnpmBuild)
}

tasks.named<Delete>("clean") {
  delete(vitepressCacheDir)
}
