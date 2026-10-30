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

package io.github.pshevche.spockk.runtime.model

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.where
import org.spockframework.runtime.SpecInfoBuilder
import org.spockframework.runtime.model.SpecInfo
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class SpecInfoHierarchySpec : Specification() {

  open class Grandparent : Specification()

  open class Parent : Grandparent()

  open class Middle : Parent()

  open class Child : Middle()

  class Grandchild : Child()

  @Shared
  private lateinit var grandparent: SpecInfo

  @Shared
  private lateinit var grandchild: SpecInfo

  @Shared
  private lateinit var hierarchy: List<SpecInfo>

  @Shared
  private val hierarchyNames = listOf("grandparent", "parent", "middle", "child", "grandchild")

  fun setupSpec() {
    hierarchy = SpecInfoBuilder(Grandchild::class.java).build().specsTopToBottom
    grandparent = hierarchy.first()
    grandchild = hierarchy.last()
  }

  @Unroll("#name")
  @MigratedFrom("org.spockframework.runtime.model.SpecInfoHierarchySpec#top spec")
  fun `top spec`(underTest: SpecInfo, name: String) {
    expect
    underTest.topSpec == grandparent

    where
    variable(underTest).from(hierarchy)
    variable(name).from(hierarchyNames)
  }

  @Unroll("#name")
  @MigratedFrom("org.spockframework.runtime.model.SpecInfoHierarchySpec#bottom spec")
  fun `bottom spec`(underTest: SpecInfo, name: String) {
    expect
    underTest.bottomSpec == grandchild

    where
    variable(underTest).from(hierarchy)
    variable(name).from(hierarchyNames)
  }

  @Unroll("#name")
  @MigratedFrom("org.spockframework.runtime.model.SpecInfoHierarchySpec#specs top to bottom")
  fun `specs top to bottom`(underTest: SpecInfo, name: String) {
    expect
    underTest.specsTopToBottom == hierarchy

    where
    variable(underTest).from(hierarchy)
    variable(name).from(hierarchyNames)
  }

  @Unroll("#name")
  @MigratedFrom("org.spockframework.runtime.model.SpecInfoHierarchySpec#specs bottom to top")
  fun `specs bottom to top`(underTest: SpecInfo, name: String) {
    expect
    underTest.specsBottomToTop == hierarchy.reversed()

    where
    variable(underTest).from(hierarchy)
    variable(name).from(hierarchyNames)
  }

  @Unroll("#name")
  @MigratedFrom("org.spockframework.runtime.model.SpecInfoHierarchySpec#specs to bottom")
  fun `specs to bottom`(underTest: SpecInfo, name: String, index: Int) {
    expect
    underTest.specsCurrentToBottom == hierarchy.subList(index, hierarchy.size)

    where
    variable(underTest).from(hierarchy)
    variable(name).from(hierarchyNames)
    variable(index).from((0..4).toList())
  }

  @Unroll("#name")
  @MigratedFrom("org.spockframework.runtime.model.SpecInfoHierarchySpec#specs to top")
  fun `specs to top`(underTest: SpecInfo, name: String, index: Int) {
    expect
    underTest.specsCurrentToTop == hierarchy.subList(0, index + 1).reversed()

    where
    variable(underTest).from(hierarchy)
    variable(name).from(hierarchyNames)
    variable(index).from((0..4).toList())
  }
}
