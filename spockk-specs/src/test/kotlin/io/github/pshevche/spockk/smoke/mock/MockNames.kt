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

package io.github.pshevche.spockk.smoke.mock

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.mock.MockUtil
import spock.lang.PendingFeature
import spock.lang.Specification

class MockNames : Specification() {

  val fieldMock: MutableList<String> = Mock(MutableList::class.java) as MutableList<String>
  lateinit var fieldMock2: MutableList<String>

  fun setup() {
    fieldMock2 = Mock(MutableList::class.java) as MutableList<String>
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/694")
  @MigratedFrom("org.spockframework.smoke.mock.MockNames#a mock's name is inferred from the variable it is assigned to at the point of its creation")
  fun `a mock's name is inferred from the variable it is assigned to at the point of its creation`() {
    `when`
    val variableMock: MutableList<String> = Mock(MutableList::class.java) as MutableList<String>
    lateinit var variableMock2: MutableList<String>
    variableMock2 = Mock(MutableList::class.java) as MutableList<String>

    then
    MockUtil().asMock(fieldMock)!!.name == "fieldMock"
    MockUtil().asMock(fieldMock2)!!.name == "fieldMock2"
    MockUtil().asMock(variableMock)!!.name == "variableMock"
    MockUtil().asMock(variableMock2)!!.name == "variableMock2"
  }

  @MigratedFrom("org.spockframework.smoke.mock.MockNames#if a mock isn't assigned to a variable at the point of its creation, it doesn't have a name")
  fun `if a mock isn't assigned to a variable at the point of its creation, it doesn't have a name`() {
    `when`
    val mock = MockUtil().asMock(callMockInline())

    then
    mock!!.name == null
  }

  private fun callMockInline(): Any = Mock(MutableList::class.java)!!
}
