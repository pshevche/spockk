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

package io.github.pshevche.spockk.smoke.parameterization

import groovy.lang.Closure
import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.ClassLevelRollupSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.ClassLevelUnrollOnlySpec
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.ClassLevelUnrollSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.CustomNameProviderSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.MethodLevelUnrollSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.UnrolledDataVariablesPatternSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.UnrolledDataVariablesWithIndexOnlyPatternSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.UnrolledDataVariablesWithIndexPatternSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.UnrolledDefaultNamingSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.UnrolledFailingProviderSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.UnrolledFeatureNameAndIndexPatternSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.UnrolledNullVariablePatternSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.UnrolledPatternSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.UnrolledPropertyExpressionPatternSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.UnrolledSimpleSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.UnrolledUnevaluablePatternSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.UnrolledZeroArgMethodPatternSpec
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.codehaus.groovy.runtime.InvokerHelper
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.EngineExecutionResults
import org.junit.platform.testkit.engine.EngineTestKit
import org.spockframework.builder.DelegatingScript
import org.spockframework.runtime.GroovyRuntimeUtil
import org.spockframework.runtime.RunContext
import org.spockframework.util.IThrowableFunction
import spock.lang.Specification

class UnrolledFeatureMethods : Specification() {

  @MigratedFrom("org.spockframework.smoke.parameterization.UnrolledFeatureMethods#iterations of an unrolled feature count as separate tests")
  fun `iterations of an unrolled feature count as separate tests`() {
    `when`
    val events = EngineTestKitUtils.execute(selectClass(UnrolledSimpleSpec::class.java))

    then
    events.succeeded().count() == 4L
    events.failed().count() == 0L
    events.skipped().count() == 0L
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.UnrolledFeatureMethods#if creation of a data provider fails, feature isn't unrolled")
  fun `if creation of a data provider fails, feature isn't unrolled`() {
    `when`
    val results = EngineRuns.run(selectClass(UnrolledFailingProviderSpec::class.java))
    val failedContainerNames = results.containerEvents().failed().list().map { it.testDescriptor.displayName }

    then
    results.testEvents().succeeded().count() == 0L
    results.testEvents().failed().count() == 1L
    results.testEvents().skipped().count() == 0L
    results.containerEvents().started().count() == 1L + 1L + 1L // engine + spec + unrolled feature
    results.containerEvents().failed().count() == 1L
    failedContainerNames == listOf("a feature")
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.UnrolledFeatureMethods#iterations of an unrolled feature foo are named 'foo [x: 1, #0]', 'foo [x: 2, #1]', etc."
  )
  fun `iterations of an unrolled feature foo are named foo (x 1, #0), foo (x 2, #1), etc`() {
    `when`
    val names = EngineRuns.startedNames(UnrolledSimpleSpec::class.java)

    then
    names == listOf("foo", "foo [x: 1, #0]", "foo [x: 2, #1]", "foo [x: 3, #2]")
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.UnrolledFeatureMethods#naming pattern may refer to data variables")
  fun `naming pattern may refer to data variables`() {
    `when`
    val names = EngineRuns.startedNames(UnrolledPatternSpec::class.java)

    then
    names == listOf("foo", "one a two 1 three", "one b two 2 three", "one c two 3 three")
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.UnrolledFeatureMethods#naming pattern may refer to feature name and iteration index")
  fun `naming pattern may refer to feature name and iteration index`() {
    `when`
    val names = EngineRuns.startedNames(UnrolledFeatureNameAndIndexPatternSpec::class.java)

    then
    names == listOf("foo", "one foo two 0 three", "one foo two 1 three", "one foo two 2 three")
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.UnrolledFeatureMethods#naming pattern may refer to dataVariables")
  fun `naming pattern may refer to dataVariables`() {
    `when`
    val names = EngineRuns.startedNames(UnrolledDataVariablesPatternSpec::class.java)

    then
    names == listOf("foo", "one x: 1, y: a two", "one x: 2, y: b two", "one x: 3, y: c two")
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.UnrolledFeatureMethods#naming pattern may refer to dataVariablesWithIndex")
  fun `naming pattern may refer to dataVariablesWithIndex`() {
    `when`
    val names = EngineRuns.startedNames(UnrolledDataVariablesWithIndexPatternSpec::class.java)

    then
    names == listOf("foo", "one x: 1, y: a, #0 two", "one x: 2, y: b, #1 two", "one x: 3, y: c, #2 two")
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.UnrolledFeatureMethods#old iteration naming is restorable using configuration script")
  fun `old iteration naming is restorable using configuration script`() {
    given
    val script = EngineRuns.unrollScript("defaultPattern" to "#featureName[#iterationIndex]")

    `when`
    val names = EngineRuns.startedNames(UnrolledDefaultNamingSpec::class.java, script)

    then
    names == listOf("foo", "foo[0]", "foo[1]", "foo[2]")
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.UnrolledFeatureMethods#default iteration naming is configurable using configuration script"
  )
  fun `default iteration naming is configurable using configuration script`() {
    given
    val script = EngineRuns.unrollScript("defaultPattern" to "#featureName")

    `when`
    val names = EngineRuns.startedNames(UnrolledDefaultNamingSpec::class.java, script)

    then
    names == listOf("foo", "foo", "foo", "foo")
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.UnrolledFeatureMethods#includeFeatureNameForIterations is configurable using configuration script"
  )
  fun `includeFeatureNameForIterations is configurable using configuration script`() {
    given
    val script = EngineRuns.unrollScript("includeFeatureNameForIterations" to false)

    `when`
    val names = EngineRuns.startedNames(UnrolledDefaultNamingSpec::class.java, script)

    then
    names == listOf("foo", "x: 1, y: a, #0", "x: 2, y: b, #1", "x: 3, y: c, #2")
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.UnrolledFeatureMethods#dataVariablesWithIndex can be used in @Unroll")
  fun `dataVariablesWithIndex can be used in @Unroll`() {
    `when`
    val names = EngineRuns.startedNames(UnrolledDataVariablesWithIndexOnlyPatternSpec::class.java)

    then
    names == listOf("foo", "x: 1, y: a, #0", "x: 2, y: b, #1", "x: 3, y: c, #2")
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.UnrolledFeatureMethods#variables in naming pattern whose value is null are replaced correctly")
  fun `variables in naming pattern whose value is null are replaced correctly`() {
    `when`
    val names = EngineRuns.startedNames(UnrolledNullVariablePatternSpec::class.java)

    then
    names == listOf("foo", "one 1 two null three")
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.UnrolledFeatureMethods#naming pattern supports property expressions")
  fun `naming pattern supports property expressions`() {
    `when`
    val names = EngineRuns.startedNames(UnrolledPropertyExpressionPatternSpec::class.java)

    then
    names == listOf("foo", "one fred two")
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.UnrolledFeatureMethods#naming pattern supports zero-arg method calls")
  fun `naming pattern supports zero-arg method calls`() {
    `when`
    val names = EngineRuns.startedNames(UnrolledZeroArgMethodPatternSpec::class.java)

    then
    names == listOf("foo", "one 4 two")
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.UnrolledFeatureMethods#expressions in naming pattern that can't be evaluated are prefixed with '#Error:' if expressions asserting is disabled"
  )
  fun `expressions in naming pattern that can't be evaluated are prefixed with #Error if expressions asserting is disabled`() {
    given
    val script = EngineRuns.unrollScript("validateExpressions" to false)

    `when`
    val names = EngineRuns.startedNames(UnrolledUnevaluablePatternSpec::class.java, script)

    then
    names == listOf("foo", "#Error:obj ok #Error:obj.bang() #Error:obj.missing() #Error:missing")
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.UnrolledFeatureMethods#method-level unroll annotation wins over class-level annotation"
  )
  fun `method-level unroll annotation wins over class-level annotation`() {
    `when`
    val names = EngineRuns.startedNames(ClassLevelUnrollSpec::class.java)

    then
    names == listOf("method", "fred")
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.UnrolledFeatureMethods#method-level unroll annotation wins over class-level uproll annotation"
  )
  fun `method-level unroll annotation wins over class-level uproll annotation`() {
    `when`
    val names = EngineRuns.startedNames(ClassLevelRollupSpec::class.java)

    then
    names == listOf("method", "fred")
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.UnrolledFeatureMethods#method-level unroll annotation wins over configuration script")
  fun `method-level unroll annotation wins over configuration script`() {
    given
    val script = EngineRuns.unrollScript("unrollByDefault" to false)

    `when`
    val names = EngineRuns.startedNames(MethodLevelUnrollSpec::class.java, script)

    then
    names == listOf("method", "fred")
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.UnrolledFeatureMethods#class-level unroll annotation wins over configuration script")
  fun `class-level unroll annotation wins over configuration script`() {
    given
    val script = EngineRuns.unrollScript("unrollByDefault" to false)

    `when`
    val names = EngineRuns.startedNames(ClassLevelUnrollOnlySpec::class.java, script)

    then
    names == listOf("method", "fred")
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.UnrolledFeatureMethods#custom name provider for both simple and parameterized features"
  )
  fun `custom name provider for both simple and parameterized features`() {
    `when`
    val names = EngineRuns.startedNames(CustomNameProviderSpec::class.java)

    then
    names == listOf("test [suffix]", "parameterized [suffix]", "parameterized [t: 0, #0] [suffix]", "parameterized [t: 1, #1] [suffix]")
  }
}

private object EngineRuns {
  fun startedNames(spec: Class<*>, script: DelegatingScript? = null): List<String> =
    run(selectClass(spec), script).testEvents().started().list().map { it.testDescriptor.displayName }

  fun run(selector: DiscoverySelector, script: DelegatingScript? = null): EngineExecutionResults =
    RunContext.withNewContext<EngineExecutionResults, RuntimeException>(
      "UnrolledFeatureMethods",
      RunContext.get().spockUserHome,
      script,
      emptyList(),
      emptyList(),
      true,
      IThrowableFunction { EngineTestKit.engine("spock").selectors(selector).execute() }
    )

  // Equivalent of the config script `unroll { <key> <value> ... }`, built by hand because the Groovy shell
  // on the test classpath cannot compile a script under Spock's AST transform.
  fun unrollScript(vararg settings: Pair<String, Any?>): DelegatingScript = object : DelegatingScript() {
    private var configDelegate: Any? = null

    override fun `$setDelegate`(delegate: Any?) {
      configDelegate = delegate
      super.`$setDelegate`(delegate)
    }

    override fun run(): Any? {
      val block = object : Closure<Any?>(this) {
        @Suppress("unused")
        fun doCall(config: Any?): Any? {
          settings.forEach { (key, value) -> InvokerHelper.setProperty(config, key, value) }
          return null
        }
      }
      return GroovyRuntimeUtil.invokeMethod(configDelegate, "unroll", block)
    }
  }
}
