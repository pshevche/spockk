---
layout: page
title: Getting Started
---

<div class="page-container vp-doc">

# Getting Started

::: tip Fastest way in
Clone the [Spockk Example Project](https://github.com/pshevche/spockk-example): it's a fully configured Gradle project with a working spec, ready to run.
:::

**Prerequisites:** JDK 21+ and Kotlin 2.0.0+.

Setting up an existing project takes three small steps.

<div class="step-list">

1. ### Add the Spockk Core dependency

   The **Spockk Core** module defines the specification syntax for Kotlin and bundles Spock itself, so you don't need to declare it as a separate dependency.

   <CodeWindow title="build.gradle.kts">

   ```kotlin
   dependencies {
       testImplementation("io.github.pshevche.spockk:spockk-core:{revnumber}")
       testRuntimeOnly("org.junit.platform:junit-platform-launcher:{junitPlatformVersion}")
   }

   tasks.named<Test>("test") {
       useJUnitPlatform()
   }
   ```

   </CodeWindow>

2. ### Apply the Spockk Gradle plugin

   The **Spockk Gradle plugin** wires up the Kotlin compiler plugin that transforms Spockk's expressive syntax into runnable Spock feature methods during compilation. Without it, `given`/`when`/`then` and other constructs are just regular function calls.

   <CodeWindow title="build.gradle.kts">

   ```kotlin
   plugins {
       id("io.github.pshevche.spockk") version "{revnumber}"
   }
   ```

   </CodeWindow>

3. ### Install the Spockk IntelliJ plugin

   Install the **Spockk** plugin from the [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/29021-spockk). It teaches IntelliJ to recognize Spockk syntax: correct highlighting for block labels, formatting-aware data tables, and gutter icons to run individual specs or features straight from the editor via Gradle.

4. ### Run your first spec

   That's it: no separate spec runner, no extra configuration. Standard Gradle test tasks pick up Spockk specs automatically.

   <CodeWindow title="zsh">

   ```sh
   $ ./gradlew test

   > Task :test

   BridgeOperationsSpec > raising shields under attack PASSED

   BUILD SUCCESSFUL in 2s
   ```

   </CodeWindow>

</div>

<div class="page-nav">
  <a class="page-nav-next" href="examples">
    <span class="page-nav-label">Next</span>
    <span class="page-nav-title">Examples →</span>
  </a>
</div>

</div>
