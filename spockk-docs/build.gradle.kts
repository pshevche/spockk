import org.asciidoctor.gradle.jvm.AsciidoctorTask

repositories {
    mavenCentral()
}

plugins {
    alias(libs.plugins.asciidoctor)
}

tasks.named("asciidoctor", AsciidoctorTask::class) {
    notCompatibleWithConfigurationCache("issue in org.asciidoctor.jvm.convert")

    baseDirFollowsSourceDir()
    sourceDir(layout.projectDirectory.dir("docs"))
    setOutputDir(layout.buildDirectory.dir("docs"))
    resources {
        from(layout.projectDirectory.files("docs/images")) {
            into("images")
        }
    }
    attributes(
        mapOf(
            "imagesdir" to "images",
            "revnumber" to project.property("version"),
            "junitPlatformVersion" to libs.versions.junit.platform.get(),
            "spockVersion" to libs.versions.spock.get()
        )
    )
}

tasks.named("build") {
    dependsOn("asciidoctor")
}
