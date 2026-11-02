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
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.failureMessage
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.TooFewInvocationsAllUnsatisfiedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.TooFewInvocationsDollarMethodNameSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.TooFewInvocationsDollarPropertyNameSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.TooFewInvocationsDumpRenderingSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.TooFewInvocationsMultidimensionalArraySpec
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.TooFewInvocationsNoUnmatchedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.TooFewInvocationsOrderedBySimilaritySpec
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.TooFewInvocationsOuterScopeSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.TooFewInvocationsPropertyNameSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.TooFewInvocationsStringArgumentSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.TooFewInvocationsTargetSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.TooFewInvocationsUnverifiedInvocationsSpec
import io.github.pshevche.spockk.lang.expect
import spock.lang.PendingFeature
import spock.lang.Specification

private val EMPTY_LINE = Regex("^ ++$", RegexOption.MULTILINE)

private fun normalize(text: String): String = EMPTY_LINE.replace(text.replace("\r\n", "\n").trim(), "")

private const val D = "$"

class TooFewInvocations : Specification() {

  @MigratedFrom("org.spockframework.smoke.mock.TooFewInvocations#shows unmatched invocations, ordered by similarity")
  fun `shows unmatched invocations, ordered by similarity`() {
    expect
    normalize(failureMessage(TooFewInvocationsOrderedBySimilaritySpec::class)) == normalize(
      """
Too few invocations for:

1 * list.add(2)   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * list.add(1)
One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   1        |  2
            false
2 * list.remove(2)
methodName == "add"
|          |
remove     false
           6 differences (0% similarity)
           (remove)
           (add---)

1 * list2.add(2)
instance == target
|        |  |
|        |  Mock for type 'List' named 'list'
|        false
|        1 difference (97% similarity)
|        Mock for type 'List' named 'list(2)'
|        Mock for type 'List' named 'list(-)'
Mock for type 'List' named 'list2'

1 * map.size()
instance == target
|        |  |
|        |  Mock for type 'List' named 'list'
|        false
|        8 differences (75% similarity)
|        Mock for type '(Map-)' named '(map-)'
|        Mock for type '(List)' named '(list)'
Mock for type 'Map' named 'map'

methodName == "add"
|          |
size       false
           4 differences (0% similarity)
           (size)
           (add-)

<too few arguments>
"""
    )
  }

  @MigratedFrom("org.spockframework.smoke.mock.TooFewInvocations#makes it clear if no unmatched invocations exist")
  fun `makes it clear if no unmatched invocations exist`() {
    expect
    normalize(failureMessage(TooFewInvocationsNoUnmatchedSpec::class)) == normalize(
      """
Too few invocations for:

1 * list.add(any())   (0 invocations)

Unmatched invocations (ordered by similarity):

None
"""
    )
  }

  @MigratedFrom("org.spockframework.smoke.mock.TooFewInvocations#shows all unsatisfied interactions")
  fun `shows all unsatisfied interactions`() {
    expect
    normalize(failureMessage(TooFewInvocationsAllUnsatisfiedSpec::class)) == normalize(
      """
Too few invocations for:

1 * list.add(1)   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * list.add(2)
One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   2        |  1
            false
1 * list.get(2)
methodName == "add"
|          |
get        false
           3 differences (0% similarity)
           (get)
           (add)

One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   2        |  1
            false

Too few invocations for:

1 * list.get(1)   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * list.get(2)
One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   2        |  1
            false
1 * list.add(2)
methodName == "get"
|          |
add        false
           3 differences (0% similarity)
           (add)
           (get)

One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   2        |  1
            false
"""
    )
  }

  @MigratedFrom("org.spockframework.smoke.mock.TooFewInvocations#does not show unverified invocations")
  fun `does not show unverified invocations`() {
    val message = failureMessage(TooFewInvocationsUnverifiedInvocationsSpec::class)

    expect
    message.contains("1 * list.add(1)")
    !message.contains("1 * list2.add(2)")
  }

  @MigratedFrom("org.spockframework.smoke.mock.TooFewInvocations#does not show invocations in outer scopes")
  fun `does not show invocations in outer scopes`() {
    val message = failureMessage(TooFewInvocationsOuterScopeSpec::class)

    expect
    !message.contains("list.add(1)")
  }

  @MigratedFrom("org.spockframework.smoke.mock.TooFewInvocations#can describe string argument mismatch")
  fun `can describe string argument mismatch`() {
    expect
    normalize(failureMessage(TooFewInvocationsStringArgumentSpec::class)) == normalize(
      """
Too few invocations for:

1 * fred.wife("Wilma", "Flintstone", 30, "Bedrock")   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * fred.wife('Fred', 'Flintstone', 30, 'Quarry')
One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   Fred     |  Wilma
            false
            5 differences (0% similarity)
            (Fred-)
            (Wilma)
1: <matches>
2: <matches>
3: argument == expected
   |        |  |
   Quarry   |  Bedrock
            false
            6 differences (14% similarity)
            (Qua)r(ry-)
            (Bed)r(ock)
"""
    )
  }

  @MigratedFrom("org.spockframework.smoke.mock.TooFewInvocations#can describe method name mismatch when the method name contains a dollar sign")
  fun `can describe method name mismatch when the method name contains a dollar sign`() {
    expect
    normalize(failureMessage(TooFewInvocationsDollarMethodNameSpec::class)) == normalize(
      """
Too few invocations for:

1 * mock.`compareTo$D`(any())   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * mock.compareTo('a')
methodName == "compareTo\$D"
|          |
compareTo  false
           1 difference (90% similarity)
           compareTo(-)
           compareTo($D)
"""
    )
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/984")
  @MigratedFrom("org.spockframework.smoke.mock.TooFewInvocations#can describe property name mismatch when the property name contains a dollar sign")
  fun `can describe property name mismatch when the property name contains a dollar sign`() {
    expect
    normalize(failureMessage(TooFewInvocationsDollarPropertyNameSpec::class)) == normalize(
      """
Too few invocations for:

1 * mock.`bar$D`   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * mock.getFoo()
propertyName == "bar\$D"
|            |
foo          false
             4 differences (0% similarity)
             (foo-)
             (bar$D)
"""
    )
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/984")
  @MigratedFrom("org.spockframework.smoke.mock.TooFewInvocations#can describe property name mismatch")
  fun `can describe property name mismatch`() {
    expect
    normalize(failureMessage(TooFewInvocationsPropertyNameSpec::class)) == normalize(
      """
Too few invocations for:

1 * fred.name   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * fred.getAge()
propertyName == "name"
|            |
age          false
             2 differences (50% similarity)
             (-)a(g)e
             (n)a(m)e
"""
    )
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/984")
  @MigratedFrom("org.spockframework.smoke.mock.TooFewInvocations#can describe target mismatch")
  fun `can describe target mismatch`() {
    expect
    normalize(failureMessage(TooFewInvocationsTargetSpec::class)) == normalize(
      """
Too few invocations for:

1 * barney.name   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * fred.getName()
instance == target
|        |  |
|        |  Mock for type 'TooFewInvocationsPerson' named 'barney'
|        false
|        4 differences (92% similarity)
|        Mock for type 'TooFewInvocationsPerson' named '(f-)r(-)e(d)'
|        Mock for type 'TooFewInvocationsPerson' named '(ba)r(n)e(y)'
Mock for type 'TooFewInvocationsPerson' named 'fred'
"""
    )
  }

  @MigratedFrom("org.spockframework.smoke.mock.TooFewInvocations#renders with dump() if no better toString exists")
  fun `renders with dump() if no better toString exists`() {
    val message = failureMessage(TooFewInvocationsDumpRenderingSpec::class)
    val dumped = Regex.escape("io.github.pshevche.spockk.fixtures.runtime.samples.mock.TooFewInvocationsDumpedPerson")

    expect
    Regex("(?m)^1 \\* list\\.add\\(<$dumped@\\w+ name=Foo age=18>\\)$").containsMatchIn(message)
  }

  @MigratedFrom("org.spockframework.smoke.mock.TooFewInvocations#correctly renders multidimensional arrays")
  fun `correctly renders multidimensional arrays`() {
    expect
    normalize(failureMessage(TooFewInvocationsMultidimensionalArraySpec::class)) == normalize(
      """
Too few invocations for:

1 * list.add("bar")   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * list.add([[1, 2], [2, 5]])
One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   |        |  bar
   |        false
   [[1, 2], [2, 5]]
"""
    )
  }
}
