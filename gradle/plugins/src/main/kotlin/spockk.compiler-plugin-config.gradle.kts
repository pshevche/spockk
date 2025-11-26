plugins {
  id("com.github.gmazzo.buildconfig")
}

val compilerPluginArtifactId = "spockk-compiler-plugin"

buildConfig {
  packageName("io.github.pshevche.spockk.compilation")
  className("SpockkCompilerPluginConfig")

  buildConfigField("String", "SPOCKK_COMPILER_PLUGIN_ID", "\"${group}:${compilerPluginArtifactId}\"")
  buildConfigField("String", "SPOCKK_COMPILER_PLUGIN_GROUP", "\"${group}\"")
  buildConfigField("String", "SPOCKK_COMPILER_ARTIFACT_ID", "\"${compilerPluginArtifactId}\"")
  buildConfigField("String", "SPOCKK_COMPILER_VERSION", "\"${version}\"")
}
