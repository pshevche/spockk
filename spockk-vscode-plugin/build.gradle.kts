import com.github.gradle.node.npm.task.NpmInstallTask
import com.github.gradle.node.npm.task.NpmTask

plugins {
  id("com.github.node-gradle.node") version "7.1.0"
  id("spockk.kotlin-library")
}

node {
  version.set("22.0.0")
  npmVersion.set("10.0.0")
  download.set(false)
  nodeProjectDir.set(layout.projectDirectory)
}

val projectVersion = project.version.toString()
val npmInstall by tasks.existing(NpmInstallTask::class)

val npmRunCompile by tasks.registering(NpmTask::class) {
  dependsOn(npmInstall)
  npmCommand.set(listOf("run", "compile"))
  args.set(listOf("--", "--noEmit", "false"))
  doFirst {
    val pkg = layout.projectDirectory.file("package.json").asFile
    pkg.writeText(pkg.readText().replace("__VERSION__", projectVersion))
  }
}

val npmRunPackage by tasks.registering(NpmTask::class) {
  dependsOn(npmRunCompile)
  npmCommand.set(listOf("run", "package"))
}

val npmTest by tasks.registering(NpmTask::class) {
  dependsOn(npmRunCompile)
  npmCommand.set(listOf("run", "test"))
}

tasks.assemble { dependsOn(npmRunCompile) }
tasks.build { dependsOn(npmRunPackage) }
tasks.check { dependsOn(npmTest) }

tasks.register("cleanVsCode") {
  delete("out/", "*.vsix")
}
