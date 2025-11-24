plugins {
    alias(libs.plugins.kotlin.kapt)
    id("spockk.artifact-under-test-producer")
    id("spockk.compiler-plugin-producer")
    id("spockk.maven-central-publish")
}

dependencies {
    kapt(libs.google.autoservice)

    compileOnly(libs.google.autoservice.annotations)
    compileOnly(libs.kotlin.compiler.embeddable)
}

mavenPublishing {
    pom {
        name = "Spockk Kotlin Compiler Plugin"
        description =
            "Kotlin compiler plugin that transforms Spockk’s concise specification syntax into runnable tests."
    }
}
