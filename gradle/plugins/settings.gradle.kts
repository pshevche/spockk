dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("../libs.versions.toml"))
    }
  }
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

rootProject.name = "plugins"
