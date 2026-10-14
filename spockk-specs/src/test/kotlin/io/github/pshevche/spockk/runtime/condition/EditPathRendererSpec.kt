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
import org.spockframework.runtime.condition.EditPathRenderer
import spock.lang.Specification
import java.nio.CharBuffer

class EditPathRendererSpec : Specification() {

  private val renderer = EditPathRenderer()

  @MigratedFrom("org.spockframework.runtime.condition.EditPathRendererSpec#examples")
  fun examples(str1: String, str2: String, out1: String, out2: String) {
    given
    val dist = EditDistance(str1, str2)

    expect
    renderer.render(str1, str2, dist.calculatePath()) == "$out1\n$out2"

    where
    variables(str1, str2, out1, out2).from(
      listOf("the quick", "the quick", "the quick", "the quick"),
      listOf("the quirk", "quick", "e qui", "and now for sth. completely different"),
      listOf("the qui(c)k", "(the )quick", "(th)e qui(ck)", "(-------------)th(e) (qu----------)i(ck-----)"),
      listOf("the qui(r)k", "(----)quick", "(--)e qui(--)", "(and now for s)th(.) (completely d)i(fferent)")
    )
  }

  @MigratedFrom("org.spockframework.runtime.condition.EditPathRendererSpec#compared strings contain control characters")
  fun `compared strings contain control characters`(str1: String, str2: String, out1: String, out2: String) {
    given
    val dist = EditDistance(str1, str2)

    expect
    renderer.render(str1, str2, dist.calculatePath()) == "$out1\n$out2"

    where
    variables(str1, str2, out1, out2).from(
      listOf("one\ttwothree\u0008four\rfive\u000csix"),
      listOf("onetwo\nthreefour\rfive\tsix"),
      listOf("one(\\t)two(-~)three(\\b)four\\rfive(\\f)six"),
      listOf("one(-~)two(\\n)three(-~)four\\rfive(\\t)six")
    )
  }

  @MigratedFrom(
    "org.spockframework.runtime.condition.EditPathRendererSpec#compared strings contain same delimiters as used by diff renderer"
  )
  fun `compared strings contain same delimiters as used by diff renderer`(
    str1: String,
    str2: String,
    out1: String,
    out2: String
  ) {
    given
    val dist = EditDistance(str1, str2)

    expect
    renderer.render(str1, str2, dist.calculatePath()) == "$out1\n$out2"

    where
    variables(str1, str2, out1, out2).from(
      listOf("q(u)i(c)k("),
      listOf("q(u)i(r)k)"),
      listOf("q(u)i((c))k(()"),
      listOf("q(u)i((r))k())")
    )
  }

  @MigratedFrom("org.spockframework.runtime.condition.EditPathRendererSpec#compare String with other CharSequence")
  fun `compare String with other CharSequence`(seq: CharSequence) {
    given
    val str = "the quick brown"
    val dist = EditDistance(str, seq)

    expect
    renderer.render(str, seq, dist.calculatePath()) == "the qu(ic)k b(-)r(ow)n\nthe qu(ar)k b(u)r(--)n"

    where
    variable(seq).from(
      listOf<CharSequence>(
        "the" + " quark " + "burn",
        StringBuilder("the quark burn"),
        CharBuffer.wrap("the quark burn".toCharArray())
      )
    )
  }
}
