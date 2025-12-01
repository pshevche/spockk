plugins {
  id("com.vanniktech.maven.publish")
}

val isCI = System.getenv("CI") != null
val isFork = System.getenv("IS_FORK") == "true"

mavenPublishing {
  // TODO: enable automatic publication once the release is confirmed
  publishToMavenCentral()

  if (isCI && !isFork) {
    signAllPublications()
  }

  coordinates(group.toString(), name, version.toString())

  pom {
    inceptionYear = "2025"
    url = "https://github.com/pshevche/spockk/"
    licenses {
      license {
        name = "The Apache License, Version 2.0"
        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
        distribution = "http://www.apache.org/licenses/LICENSE-2.0.txt"
      }
    }
    developers {
      developer {
        id = "pshevche"
        name = "Pavlo Shevchenko"
        url = "https://github.com/pshevche/"
      }
    }
    scm {
      url = "https://github.com/pshevche/spockk/"
      connection = "scm:git:git://github.com/pshevche/spockk.git"
      developerConnection = "scm:git:ssh://git@github.com/pshevche/spockk.git"
    }
  }
}
