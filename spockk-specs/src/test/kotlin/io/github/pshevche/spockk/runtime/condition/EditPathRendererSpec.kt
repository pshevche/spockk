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
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.where
import org.spockframework.runtime.condition.EditDistance
import org.spockframework.runtime.condition.EditPathRenderer
import spock.lang.Specification
import java.nio.CharBuffer

class EditPathRendererSpec : Specification() {

  private val renderer = EditPathRenderer()

  @MigratedFrom("org.spockframework.runtime.condition.EditPathRendererSpec#examples")
  fun examples(str1: String, str2: String, out1: String, out2: String) {
    val dist = EditDistance(str1, str2)

    expect
    renderer.render(str1, str2, dist.calculatePath()) == "$out1\n$out2"

    where
    variable(str1).from("the quick")
    variable(str2).from("the quirk", "quick", "e qui", "and now for sth. completely different")
    variable(out1).from("the qui(c)k", "(the )quick", "(th)e qui(ck)", "(-------------)th(e) (qu----------)i(ck-----)")
    variable(out2).from("the qui(r)k", "(----)quick", "(--)e qui(--)", "(and now for s)th(.) (completely d)i(fferent)")
  }

  @MigratedFrom("org.spockframework.runtime.condition.EditPathRendererSpec#compared strings contain control characters")
  fun `compared strings contain control characters`(str1: String, str2: String, out1: String, out2: String) {
    val dist = EditDistance(str1, str2)

    expect
    renderer.render(str1, str2, dist.calculatePath()) == "$out1\n$out2"

    where
    variable(str1).from("one\ttwothree\u0008four\rfive\u000csix")
    variable(str2).from("onetwo\nthreefour\rfive\tsix")
    variable(out1).from("one(\\t)two(-~)three(\\b)four\\rfive(\\f)six")
    variable(out2).from("one(-~)two(\\n)three(-~)four\\rfive(\\t)six")
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
    val dist = EditDistance(str1, str2)

    expect
    renderer.render(str1, str2, dist.calculatePath()) == "$out1\n$out2"

    where
    variable(str1).from("q(u)i(c)k(")
    variable(str2).from("q(u)i(r)k)")
    variable(out1).from("q(u)i((c))k(()")
    variable(out2).from("q(u)i((r))k())")
  }

  @MigratedFrom("org.spockframework.runtime.condition.EditPathRendererSpec#compare String with other CharSequence")
  fun `compare String with other CharSequence`(str: String, seq: CharSequence, out1: String, out2: String) {
    val dist = EditDistance(str, seq)

    expect
    renderer.render(str, seq, dist.calculatePath()) == "$out1\n$out2"

    where
    variable(str).from("the quick brown")
    variable(seq).from(
      listOf<CharSequence>(
        "the" + " quark " + "burn",
        StringBuilder("the quark burn"),
        CharBuffer.wrap("the quark burn".toCharArray())
      )
    )
    variable(out1).from("the qu(ic)k b(-)r(ow)n")
    variable(out2).from("the qu(ar)k b(u)r(--)n")
  }
}
