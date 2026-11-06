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
import io.github.pshevche.spockk.lang.Mock
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.runtime.InvalidSpecException
import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.Specification

@Issue("https://github.com/spockframework/spock/issues/2339")
class NullTargetInteraction : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/992")
  @MigratedFrom("org.spockframework.smoke.mock.NullTargetInteraction#interaction on null target throws InvalidSpecException")
  fun `interaction on null target throws InvalidSpecException`() {
    given
    val nullObj: Any? = null

    `when`
    nullObj.toString() returns ""

    then
    val ex = thrown(InvalidSpecException::class.java)
    ex.message == "Interaction target must not be null, interactions can only be defined on mock objects"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/992")
  @MigratedFrom(
    "org.spockframework.smoke.mock.NullTargetInteraction#required interaction on null target throws InvalidSpecException"
  )
  fun `required interaction on null target throws InvalidSpecException`() {
    given
    val nullList: NullTargetCollaborator? = null

    `when`
    1 * nullList?.size()

    then
    val ex = thrown(InvalidSpecException::class.java)
    ex.message == "Interaction target must not be null, interactions can only be defined on mock objects"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/992")
  @MigratedFrom("org.spockframework.smoke.mock.NullTargetInteraction#interaction on real mock is still accepted")
  fun `interaction on real mock is still accepted`() {
    given
    val list: NullTargetCollaborator = Mock(NullTargetCollaborator::class.java)

    `when`
    list.size() returns 0

    then
    notThrown(InvalidSpecException::class.java)
    list.size() == 0
  }
}

interface NullTargetCollaborator {
  fun size(): Int
}
