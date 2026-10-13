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
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.UniqueId
import org.spockframework.runtime.ErrorSpecNode
import org.spockframework.runtime.SpecInfoBuilder
import org.spockframework.runtime.model.SpecInfo
import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Subject

class ErrorSpecNodeSpec : Specification() {

  private val tdParent = Mock(TestDescriptor::class.java)

  @Subject
  private val errorSpecNode = AccessibleErrorSpecNode(SpecInfoBuilder(javaClass).build())
    .also { it.setParent(tdParent) }

  @MigratedFrom("org.spockframework.runtime.ErrorSpecNodeSpec#should not be pruned")
  fun `should not be pruned`() {
    `when`
    errorSpecNode.prune()

    then
    0 * tdParent.removeChild(any())
  }

  @MigratedFrom("org.spockframework.runtime.ErrorSpecNodeSpec#should prevent removal of ErrorSpecNode")
  fun `should prevent removal of ErrorSpecNode`() {
    `when`
    errorSpecNode.removeFromHierarchy()

    then
    0 * tdParent.removeChild(any())
  }

  @Issue("https://github.com/spockframework/spock/issues/1444")
  @MigratedFrom("org.spockframework.runtime.ErrorSpecNodeSpec#make TestDescriptor think that an ErrorSpecNode contains tests")
  fun `make TestDescriptor think that an ErrorSpecNode contains tests`() {
    val errorSpecNodeSpy = Spy(errorSpecNode)

    `when`
    val containsTests = TestDescriptor.containsTests(errorSpecNodeSpy)

    then
    containsTests
    1 * errorSpecNodeSpy.mayRegisterTests()
  }

  // ErrorSpecNode's constructor is protected; Groovy ignores that, Kotlin does not.
  open class AccessibleErrorSpecNode(specInfo: SpecInfo) : ErrorSpecNode(UniqueId.forEngine("test"), null, specInfo, null)
}
