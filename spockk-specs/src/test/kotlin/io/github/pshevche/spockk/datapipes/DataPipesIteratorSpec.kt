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

package io.github.pshevche.spockk.datapipes

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.datapipes.CurrentDataProvider
import io.github.pshevche.spockk.fixtures.runtime.samples.datapipes.DataPipeIteratorFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.datapipes.TestDataCollection
import io.github.pshevche.spockk.fixtures.runtime.samples.datapipes.TestDataIterable
import io.github.pshevche.spockk.fixtures.runtime.samples.datapipes.TestDataIterableWithSize
import io.github.pshevche.spockk.fixtures.runtime.samples.datapipes.TestDataIterator
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.PendingFeature
import spock.lang.Specification

class DataPipesIteratorSpec : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/650")
  @MigratedFrom(
    "org.spockframework.datapipes.DataPipesIteratorSpec#Collection data provider will use size method to estimate number of iterations"
  )
  fun `Collection data provider will use size method to estimate number of iterations`() {
    `when`
    val dataCollection = CurrentDataProvider.set(TestDataCollection())
    val events = execute(selectClass(DataPipeIteratorFixture::class.java))

    then
    events.assertStatistics { it.succeeded(2) }
    dataCollection.iteratorCalls == 1
    dataCollection.sizeCalls == 1
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/650")
  @MigratedFrom(
    "org.spockframework.datapipes.DataPipesIteratorSpec#Iterable uses the Groovy default size method to estimate number of iterations"
  )
  fun `Iterable uses the Groovy default size method to estimate number of iterations`() {
    `when`
    val dataIterable = CurrentDataProvider.set(TestDataIterable())
    val events = execute(selectClass(DataPipeIteratorFixture::class.java))

    then
    dataIterable.iteratorCalls == 2
    events.assertStatistics { it.succeeded(2) }
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/650")
  @MigratedFrom(
    "org.spockframework.datapipes.DataPipesIteratorSpec#Iterable with size method shall use the size method to estimate number of iterations"
  )
  fun `Iterable with size method shall use the size method to estimate number of iterations`() {
    `when`
    val dataIterable = CurrentDataProvider.set(TestDataIterableWithSize())
    val events = execute(selectClass(DataPipeIteratorFixture::class.java))

    then
    events.assertStatistics { it.succeeded(2) }
    dataIterable.iteratorCalls == 1
    dataIterable.sizeCalls == 1
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/650")
  @MigratedFrom(
    "org.spockframework.datapipes.DataPipesIteratorSpec#Iterator shall be only called once"
  )
  fun `Iterator shall be only called once`() {
    `when`
    val dataIterator = CurrentDataProvider.set(TestDataIterator())
    val events = execute(selectClass(DataPipeIteratorFixture::class.java))

    then
    events.assertStatistics { it.succeeded(2) }
    dataIterator.hasNextCalls == 3
    dataIterator.nextCalls == 1
  }
}
