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

package io.github.pshevche.spockk.runtime

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.Stub
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import org.junit.platform.engine.EngineExecutionListener
import org.spockframework.runtime.DataIteratorFactory
import org.spockframework.runtime.ErrorInfoCollector
import org.spockframework.runtime.IRunSupervisor
import org.spockframework.runtime.RunContext
import org.spockframework.runtime.SpockExecutionContext
import org.spockframework.runtime.model.DataProcessorMetadata
import org.spockframework.runtime.model.DataProviderInfo
import org.spockframework.runtime.model.DataProviderMetadata
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.MethodInfo
import spock.config.RunnerConfiguration
import spock.lang.Specification

class EstimatedNumberOfIterations : Specification() {

  private val listener: EngineExecutionListener = Stub(EngineExecutionListener::class.java)

  private val factory = DataIteratorFactory(Stub(IRunSupervisor::class.java))

  @MigratedFrom("org.spockframework.runtime.EstimatedNumberOfIterations#w/o data provider")
  fun `without data provider`() {
    given
    val runContext = Stub(RunContext::class.java) {
      getConfiguration(RunnerConfiguration::class.java) returns RunnerConfiguration()
    }
    val featureInfo = Stub(FeatureInfo::class.java) {
      getDataProcessorMethod() returns methodInfoFor("dataProcessor1")
      getDataProviders() returns emptyList<DataProviderInfo>()
      getDataVariableMultiplicationsMethod() returns null
    }

    expect("estimation is 1")
    factory.createFeatureDataIterator(contextFor(runContext).withCurrentFeature(featureInfo)).estimatedNumIterations == 1
  }

  @DataProcessorMetadata(dataVariables = [])
  fun dataProcessor1(args: Array<Any?>): Array<Any?> = arrayOfNulls(0)

  @MigratedFrom("org.spockframework.runtime.EstimatedNumberOfIterations#w/ data provider that doesn't respond to size")
  fun `with data provider that doesn't respond to size`() {
    given
    val runContext = Stub(RunContext::class.java) {
      getConfiguration(RunnerConfiguration::class.java) returns RunnerConfiguration()
    }
    val featureInfo = Stub(FeatureInfo::class.java) {
      getDataProcessorMethod() returns methodInfoFor("dataProcessor2")
      getDataProviders() returns listOf(providerInfo("a", "dataProvider2"))
      getDataVariableMultiplicationsMethod() returns null
    }

    expect("estimation is 'unknown', represented as -1")
    factory.createFeatureDataIterator(contextFor(runContext).withCurrentFeature(featureInfo)).estimatedNumIterations == -1
  }

  @DataProviderMetadata(line = -1, dataVariables = ["a"])
  fun dataProvider2(): Iterator<String> = listOf("a").iterator()

  @DataProcessorMetadata(dataVariables = ["a"])
  fun dataProcessor2(args: Array<Any?>): Array<Any?> = args

  @MigratedFrom("org.spockframework.runtime.EstimatedNumberOfIterations#w/ data provider that responds to size")
  fun `with data provider that responds to size`() {
    given
    val runContext = Stub(RunContext::class.java) {
      getConfiguration(RunnerConfiguration::class.java) returns RunnerConfiguration()
    }
    val featureInfo = Stub(FeatureInfo::class.java) {
      getDataProcessorMethod() returns methodInfoFor("dataProcessor3")
      getDataProviders() returns listOf(providerInfo("a", "dataProvider3"))
      getDataVariableMultiplicationsMethod() returns null
    }

    expect("estimation is size")
    factory.createFeatureDataIterator(contextFor(runContext).withCurrentFeature(featureInfo)).estimatedNumIterations == 3
  }

  @DataProviderMetadata(line = -1, dataVariables = ["a"])
  fun dataProvider3(): List<String> = listOf("a", "b", "c")

  @DataProcessorMetadata(dataVariables = ["a"])
  fun dataProcessor3(args: Array<Any?>): Array<Any?> = args

  @MigratedFrom("org.spockframework.runtime.EstimatedNumberOfIterations#w/ multiple data providers, all of which respond to size")
  fun `with multiple data providers, all of which respond to size`() {
    given
    val runContext = Stub(RunContext::class.java) {
      getConfiguration(RunnerConfiguration::class.java) returns RunnerConfiguration()
    }
    val featureInfo = Stub(FeatureInfo::class.java) {
      getDataProcessorMethod() returns methodInfoFor("dataProcessor4")
      getDataProviders() returns listOf(
        providerInfo("a", "dataProvider4_1"),
        providerInfo("b", "dataProvider4_2"),
        providerInfo("c", "dataProvider4_3")
      )
      getDataVariableMultiplicationsMethod() returns null
    }

    expect("estimation is minimum")
    factory.createFeatureDataIterator(contextFor(runContext).withCurrentFeature(featureInfo)).estimatedNumIterations == 1
  }

  @DataProviderMetadata(line = -1, dataVariables = ["a"])
  fun dataProvider4_1(): List<String> = listOf("a")

  @DataProviderMetadata(line = -1, dataVariables = ["b"])
  fun dataProvider4_2(): IntRange = 1..3

  @DataProviderMetadata(line = -1, dataVariables = ["c"])
  fun dataProvider4_3(): Iterable<Int> = listOf(1, 2)

  @DataProcessorMetadata(dataVariables = ["a", "b", "c"])
  fun dataProcessor4(args: Array<Any?>): Array<Any?> = args

  @MigratedFrom("org.spockframework.runtime.EstimatedNumberOfIterations#w/ multiple data providers, one of which doesn't respond to size")
  fun `with multiple data providers, one of which doesn't respond to size`() {
    given
    val runContext = Stub(RunContext::class.java) {
      getConfiguration(RunnerConfiguration::class.java) returns RunnerConfiguration()
    }
    val featureInfo = Stub(FeatureInfo::class.java) {
      getDataProcessorMethod() returns methodInfoFor("dataProcessor5")
      getDataProviders() returns listOf(
        providerInfo("a", "dataProvider5_1"),
        providerInfo("b", "dataProvider5_2"),
        providerInfo("c", "dataProvider5_3")
      )
      getDataVariableMultiplicationsMethod() returns null
    }

    expect("estimation is minimum of others")
    factory.createFeatureDataIterator(contextFor(runContext).withCurrentFeature(featureInfo)).estimatedNumIterations == 2
  }

  @DataProviderMetadata(line = -1, dataVariables = ["a"])
  fun dataProvider5_1(): Iterator<String> = listOf("a").iterator()

  @DataProviderMetadata(line = -1, dataVariables = ["b"])
  fun dataProvider5_2(): IntRange = 1..3

  @DataProviderMetadata(line = -1, dataVariables = ["c"])
  fun dataProvider5_3(): Iterable<Int> = listOf(1, 2)

  @DataProcessorMetadata(dataVariables = ["a", "b", "c"])
  fun dataProcessor5(args: Array<Any?>): Array<Any?> = args

  private fun contextFor(runContext: RunContext) = SpockExecutionContext(listener)
    .withErrorInfoCollector(ErrorInfoCollector())
    .withCurrentInstance(this)
    .withRunContext(runContext)

  private fun providerInfo(variable: String, providerMethod: String) = DataProviderInfo().apply {
    dataVariables = listOf(variable)
    previousDataTableVariables = emptyList()
    dataProviderMethod = methodInfoFor(providerMethod)
  }

  private fun methodInfoFor(methodName: String) = MethodInfo().apply {
    reflection = EstimatedNumberOfIterations::class.java.declaredMethods.first { it.name == methodName }
  }
}
