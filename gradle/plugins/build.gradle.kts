plugins {
  `kotlin-dsl`
}

fun Provider<PluginDependency>.asDependency(): Provider<String> =
  this.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }

dependencies {
  implementation(libs.plugins.buildconfig.asDependency())
  implementation(libs.plugins.detekt.asDependency())
  implementation(libs.plugins.kotlin.jvm.asDependency())
  implementation(libs.plugins.maven.publish.asDependency())
  implementation(libs.plugins.spotless.asDependency())
}
