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

package io.github.pshevche.spockk.runtime.condition

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.variables
import io.github.pshevche.spockk.lang.where
import org.spockframework.runtime.condition.EditDistance
import org.spockframework.runtime.condition.EditOperation
import org.spockframework.runtime.condition.EditOperation.Kind.DELETE
import org.spockframework.runtime.condition.EditOperation.Kind.INSERT
import org.spockframework.runtime.condition.EditOperation.Kind.SKIP
import org.spockframework.runtime.condition.EditOperation.Kind.SUBSTITUTE
import spock.lang.Specification
import java.util.concurrent.ThreadLocalRandom

class EditDistanceSpec : Specification() {

  @MigratedFrom("org.spockframework.runtime.condition.EditDistanceSpec#matrix for 'sitting' and 'kitten'")
  fun `matrix for 'sitting' and 'kitten'`() {
    given
    val matrix = EditDistance("sitting", "kitten").matrix.map { it.toList() }

    expect
    matrix.size == 8
    matrix[0] == listOf(0, 1, 2, 3, 4, 5, 6)
    matrix[1] == listOf(1, 1, 2, 3, 4, 5, 6)
    matrix[2] == listOf(2, 2, 1, 2, 3, 4, 5)
    matrix[3] == listOf(3, 3, 2, 1, 2, 3, 4)
    matrix[4] == listOf(4, 4, 3, 2, 1, 2, 3)
    matrix[5] == listOf(5, 5, 4, 3, 2, 2, 3)
    matrix[6] == listOf(6, 6, 5, 4, 3, 3, 2)
    matrix[7] == listOf(7, 7, 6, 5, 4, 4, 3)
  }

  @MigratedFrom("org.spockframework.runtime.condition.EditDistanceSpec#matrix for 'Sunday' and 'Saturday'")
  fun `matrix for 'Sunday' and 'Saturday'`() {
    given
    val matrix = EditDistance("Sunday", "Saturday").matrix.map { it.toList() }

    expect
    matrix.size == 7
    matrix[0] == listOf(0, 1, 2, 3, 4, 5, 6, 7, 8)
    matrix[1] == listOf(1, 0, 1, 2, 3, 4, 5, 6, 7)
    matrix[2] == listOf(2, 1, 1, 2, 2, 3, 4, 5, 6)
    matrix[3] == listOf(3, 2, 2, 2, 3, 3, 4, 5, 6)
    matrix[4] == listOf(4, 3, 3, 3, 3, 4, 3, 4, 5)
    matrix[5] == listOf(5, 4, 3, 4, 4, 4, 4, 3, 4)
    matrix[6] == listOf(6, 5, 4, 4, 5, 5, 5, 4, 3)
  }

  @MigratedFrom("org.spockframework.runtime.condition.EditDistanceSpec#matrix for 'levenshtein' and 'meilenstein'")
  fun `matrix for 'levenshtein' and 'meilenstein'`() {
    given
    val matrix = EditDistance("levenshtein", "meilenstein").matrix.map { it.toList() }

    expect
    matrix.size == 12
    matrix[0] == listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
    matrix[1] == listOf(1, 1, 2, 3, 3, 4, 5, 6, 7, 8, 9, 10)
    matrix[2] == listOf(2, 2, 1, 2, 3, 3, 4, 5, 6, 7, 8, 9)
    matrix[3] == listOf(3, 3, 2, 2, 3, 4, 4, 5, 6, 7, 8, 9)
    matrix[4] == listOf(4, 4, 3, 3, 3, 3, 4, 5, 6, 6, 7, 8)
    matrix[5] == listOf(5, 5, 4, 4, 4, 4, 3, 4, 5, 6, 7, 7)
    matrix[6] == listOf(6, 6, 5, 5, 5, 5, 4, 3, 4, 5, 6, 7)
    matrix[7] == listOf(7, 7, 6, 6, 6, 6, 5, 4, 4, 5, 6, 7)
    matrix[8] == listOf(8, 8, 7, 7, 7, 7, 6, 5, 4, 5, 6, 7)
    matrix[9] == listOf(9, 9, 8, 8, 8, 7, 7, 6, 5, 4, 5, 6)
    matrix[10] == listOf(10, 10, 9, 8, 9, 8, 8, 7, 6, 5, 4, 5)
    matrix[11] == listOf(11, 11, 10, 9, 9, 9, 8, 8, 7, 6, 5, 4)
  }

  @MigratedFrom("org.spockframework.runtime.condition.EditDistanceSpec#path from 'sitting' to 'kitten'")
  fun `path from 'sitting' to 'kitten'`() {
    given
    val path = EditDistance("sitting", "kitten").calculatePath().map { it.kind to it.length }

    expect
    path == listOf(SUBSTITUTE to 1, SKIP to 3, SUBSTITUTE to 1, SKIP to 1, DELETE to 1)
  }

  @MigratedFrom("org.spockframework.runtime.condition.EditDistanceSpec#path from 'Sunday' to 'Saturday'")
  fun `path from 'Sunday' to 'Saturday'`() {
    given
    val path = EditDistance("Sunday", "Saturday").calculatePath().map { it.kind to it.length }

    expect
    path == listOf(SKIP to 1, INSERT to 2, SKIP to 1, SUBSTITUTE to 1, SKIP to 3)
  }

  @MigratedFrom("org.spockframework.runtime.condition.EditDistanceSpec#path from 'levenshtein' to 'meilenstein'")
  fun `path from 'levenshtein' to 'meilenstein'`() {
    given
    val path = EditDistance("levenshtein", "meilenstein").calculatePath().map { it.kind to it.length }

    expect
    path == listOf(
      SUBSTITUTE to 1,
      SKIP to 1,
      SUBSTITUTE to 1,
      INSERT to 1,
      SKIP to 3,
      DELETE to 1,
      SKIP to 4
    )
  }

  @MigratedFrom("org.spockframework.runtime.condition.EditDistanceSpec#compute distance")
  fun `compute distance`(str: String, d: Int) {
    given
    val dist = EditDistance("asdf", str)

    expect
    dist.distance == d

    where
    variables(str, d).from(
      listOf("xsdf", "axdf", "asxf", "asdx", "", "a", "as", "asd", "asdf", "xasdf", "asdfx", "xasdfx"),
      listOf(1, 1, 1, 1, 4, 3, 2, 1, 0, 1, 1, 2)
    )
  }

  @MigratedFrom("org.spockframework.runtime.condition.EditDistanceSpec#computed path has correct distance")
  fun `computed path has correct distance`(num: Int) {
    given
    val str1 = randomString(num)
    val str2 = editedString(str1)
    val dist = EditDistance(str1, str2)

    expect
    computeDistance(dist.calculatePath()) == dist.distance

    where
    variable(num).from((0..99).toList())
  }

  private fun computeDistance(operations: List<EditOperation>) =
    operations.sumOf { if (it.kind == SKIP) 0 else it.length }

  private fun randomChar(): Char = CHARS[ThreadLocalRandom.current().nextInt(CHARS.size)]

  private fun randomString(length: Int) = buildString { repeat(length) { append(randomChar()) } }

  private fun editedString(str: String) = buildString {
    for (c in str) {
      when (ThreadLocalRandom.current().nextInt(4)) {
        0 -> append(c)

        1 -> append(randomChar())

        2 -> Unit

        else -> {
          append(randomChar())
          append(c)
        }
      }
    }
  }

  private companion object {
    val CHARS = ('a'..'z') + ('A'..'Z') + ('0'..'9') + List(10) { ' ' }
  }
}
