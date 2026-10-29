/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.pshevche.spockk.smoke.extension

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.and
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.cleanup
import io.github.pshevche.spockk.lang.did
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.IStandardStreamsListener
import org.spockframework.runtime.StandardStreamsCapturer
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.IterationInfo
import org.spockframework.runtime.model.MethodInfo
import org.spockframework.runtime.model.parallel.Resources
import spock.lang.ResourceLock
import spock.lang.Snapshotter
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll
import java.lang.reflect.InvocationTargetException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SnapshotterSpec : Specification() {

  @TempDir
  private lateinit var tmpDir: Path

  @MigratedFrom("org.spockframework.smoke.extension.SnapshotterSpec#Snapshotter.Store can store and load correctly (#scenario)")
  fun `Snapshotter Store can store and load correctly (#scenario)`(scenario: String, value: String) {
    given
    val updatingStore = Snapshotter.Store(specificationContext.currentIteration, tmpDir, true, false, "txt", StandardCharsets.UTF_8)

    `when`
    updatingStore.saveSnapshot("", value)
    val loaded = updatingStore.loadSnapshot("").orElseThrow { RuntimeException("Missing Snapshot") }

    then
    loaded == value

    where
    scenario ; value
    "single line" ; "some simple value"
    "with newlines at beginning and end" ; "\nsome simple value\n"
    "with newlines at beginning and end and in between" ; "\nsome\nsimple\nvalue\n"
    "with multiple newlines at beginning and end and in between" ; "\n\nsome\n\n\nsimple\n\n\n\nvalue\n\n"
  }

  @Unroll("safeName calculation [#iterationIndex]")
  @MigratedFrom("org.spockframework.smoke.extension.SnapshotterSpec#safeName calculation")
  fun `safeName calculation`(featureName: String, iterationIndex: Int, extension: String, snapshotId: String, safeName: String) {
    given
    val featureInfo = FeatureInfo().apply {
      name = featureName
      parent = specificationContext.currentSpec
      isForceParameterized = iterationIndex > 0
      featureMethod = MethodInfo().apply { reflection = FeatureMethodHolder::class.java.declaredMethods.single() }
    }
    val iterationInfo = IterationInfo(featureInfo, iterationIndex, arrayOf(), iterationIndex + 1)

    expect
    calculateSafeUniqueName(extension, iterationInfo, snapshotId) == safeName

    where
    featureName         ; iterationIndex ; extension ; snapshotId              ; safeName
    "a feature"         ; 0              ; "txt"     ; "a snapshot"            ; "a_feature-a_snapshot.txt"
    "a feature"         ; 0              ; "txt"     ; ""                      ; "a_feature.txt"
    "a feature"         ; 0              ; "groovy"  ; ""                      ; "a_feature.groovy"
    "a feature"         ; 1              ; "txt"     ; "a snapshot"            ; "a_feature-a_snapshot-[1].txt"
    "a feature"         ; 1              ; "txt"     ; ""                      ; "a_feature-[1].txt"
    "a".repeat(300)     ; 0              ; "txt"     ; ""                      ; "a".repeat(242) + "-0_1.txt"
    "a".repeat(300)     ; 100            ; "txt"     ; "a longer snapshot id"  ; "a".repeat(215) + "-a_longer_snapshot_id-0_1-[100].txt"
  }

  @MigratedFrom("org.spockframework.smoke.extension.SnapshotterSpec#snapshotId is limited to 100 characters")
  fun `snapshotId is limited to 100 characters`() {
    `when`
    calculateSafeUniqueName("txt", Stub(IterationInfo::class.java), "a".repeat(101))

    then
    val e = thrown(IllegalArgumentException::class.java)
    e.message!!.startsWith("'snapshotId' is too long, only 100 characters are allowed, but was 101: aaaaa")
  }

  @ResourceLock(Resources.SYSTEM_OUT)
  @ResourceLock(Resources.SYSTEM_ERR)
  @MigratedFrom("org.spockframework.smoke.extension.SnapshotterSpec#snapshotter stores .actual file on mismatch and deletes it on a successful match")
  fun `snapshotter stores actual file on mismatch and deletes it on a successful match`(snapshotId: String) {
    given
    var actualPath: String? = null
    val updatingSnapshotter =
      Snapshotter(Snapshotter.Store(specificationContext.currentIteration, tmpDir, true, false, "txt", StandardCharsets.UTF_8))
    val verifyingSnapshotter =
      Snapshotter(Snapshotter.Store(specificationContext.currentIteration, tmpDir, false, true, "txt", StandardCharsets.UTF_8))

    and
    val listener: IStandardStreamsListener = Mock()
    val capturer = StandardStreamsCapturer().apply {
      addStandardStreamsListener(listener)
      start()
      muteStandardStreams()
    }

    and
    updatingSnapshotter.assertThat("reference").matchesSnapshot(snapshotId)

    `when`
    verifyingSnapshotter.assertThat("different").matchesSnapshot(snapshotId)

    then
    thrown(ConditionNotSatisfiedError::class.java)
    1 * listener.standardErr(any()) did { args ->
      actualPath = Regex("Snapshot actual value has been saved to: (.*)").find(args[0] as String)?.groupValues?.get(1)
    }

    and
    actualPath != null
    val path = Paths.get(actualPath!!)
    Files.isRegularFile(path)
    Files.readString(path) == "different"

    `when`
    verifyingSnapshotter.assertThat("reference").matchesSnapshot(snapshotId)

    then
    !Files.exists(path)

    cleanup
    capturer.stop()

    where
    variable(snapshotId).from("", "my-id")
  }

  @Suppress("unused")
  private class FeatureMethodHolder {
    fun `$spock_feature_0_1`() = Unit
  }

  private fun calculateSafeUniqueName(extension: String, iteration: IterationInfo, snapshotId: String): String {
    val method = Snapshotter.Store::class.java.declaredMethods.single { it.name == "calculateSafeUniqueName" }
    method.isAccessible = true
    try {
      return method.invoke(null, extension, iteration, snapshotId) as String
    } catch (e: InvocationTargetException) {
      throw e.targetException
    }
  }
}
