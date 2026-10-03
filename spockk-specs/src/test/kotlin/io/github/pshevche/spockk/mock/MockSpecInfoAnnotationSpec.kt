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

package io.github.pshevche.spockk.mock

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.mock.MockUtil
import org.spockframework.runtime.model.NodeInfo
import org.spockframework.runtime.model.SpecInfo
import org.spockframework.util.Nullable
import spock.lang.Specification
import java.io.Serializable

interface Issue520Repository {
  fun <E, ID : Serializable> persist(e: E): ID
}

class MockSpecInfoAnnotationSpec : Specification() {

  @MigratedFrom(
    "org.spockframework.mock.MockSpecInfoAnnotationSpec#NodeInfo.getAnnotation() shall return valid Stub for Stub Issue #1163"
  )
  fun `NodeInfo getAnnotation() shall return valid Stub for Stub Issue #1163`() {
    given
    val mockUtil = MockUtil()
    val spec = Stub(SpecInfo::class.java)
    // Kotlin, unlike Groovy, statically resolves getAnnotation()'s generic return type and
    // inserts a checkcast to Nullable; reflection keeps the call as dynamic as upstream's,
    // matching the actual erased return type (Annotation) the assertions check against.
    val getAnnotation = NodeInfo::class.java.getMethod("getAnnotation", Class::class.java)

    `when`
    val t = getAnnotation.invoke(spec, Nullable::class.java)

    then
    t is Annotation
    mockUtil.isMock(t)
  }

  @MigratedFrom(
    "org.spockframework.mock.MockSpecInfoAnnotationSpec#NodeInfo.getAnnotation() shall return null for Mock Issue #1163"
  )
  fun `NodeInfo getAnnotation() shall return null for Mock Issue #1163`() {
    given
    val spec = Mock(SpecInfo::class.java)

    `when`
    val t = spec.getAnnotation(Nullable::class.java)

    then
    t == null
  }

  @MigratedFrom(
    "org.spockframework.mock.MockSpecInfoAnnotationSpec#Better support for generic return types with Stub() Issue #520"
  )
  fun `Better support for generic return types with Stub() Issue #520`() {
    `when`
    val stub = Stub(Issue520Repository::class.java)

    then
    stub.persist<Any?, Serializable>(null) is Serializable
  }
}
