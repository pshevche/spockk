/*
 * Copyright 2025 the original author or authors.
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

@file:Suppress("unused")

package io.github.pshevche.spockk.lang

fun <V> variable(v: V) = DataVariable1(v)

fun <V1, V2> variables(v1: V1, v2: V2) = DataVariable2(v1, v2)

fun <V1, V2, V3> variables(v1: V1, v2: V2, v3: V3) = DataVariable3(v1, v2, v3)

fun <V1, V2, V3, V4> variables(v1: V1, v2: V2, v3: V3, v4: V4) = DataVariable4(v1, v2, v3, v4)

fun <V1, V2, V3, V4, V5> variables(v1: V1, v2: V2, v3: V3, v4: V4, v5: V5) =
  DataVariable5(v1, v2, v3, v4, v5)

fun <V1, V2, V3, V4, V5, V6> variables(v1: V1, v2: V2, v3: V3, v4: V4, v5: V5, v6: V6) =
  DataVariable6(v1, v2, v3, v4, v5, v6)

fun <V1, V2, V3, V4, V5, V6, V7> variables(v1: V1, v2: V2, v3: V3, v4: V4, v5: V5, v6: V6, v7: V7) =
  DataVariable7(v1, v2, v3, v4, v5, v6, v7)

fun <V1, V2, V3, V4, V5, V6, V7, V8> variables(
  v1: V1,
  v2: V2,
  v3: V3,
  v4: V4,
  v5: V5,
  v6: V6,
  v7: V7,
  v8: V8
) = DataVariable8(v1, v2, v3, v4, v5, v6, v7, v8)

fun <V1, V2, V3, V4, V5, V6, V7, V8, V9> variables(
  v1: V1,
  v2: V2,
  v3: V3,
  v4: V4,
  v5: V5,
  v6: V6,
  v7: V7,
  v8: V8,
  v9: V9
) = DataVariable9(v1, v2, v3, v4, v5, v6, v7, v8, v9)

fun <V1, V2, V3, V4, V5, V6, V7, V8, V9, V10> variables(
  v1: V1,
  v2: V2,
  v3: V3,
  v4: V4,
  v5: V5,
  v6: V6,
  v7: V7,
  v8: V8,
  v9: V9,
  v10: V10
) = DataVariable10(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10)

abstract class DataVariable {
  protected fun throwIllegalInitializationUsage(): Unit =
    throw UnsupportedOperationException(
      "The data variables must be initialized only within the 'where' block of a spockk feature"
    )
}

@ConsistentCopyVisibility
data class DataVariable1<V1> internal constructor(private val v: V1) : DataVariable() {
  fun from(vararg values: V1) {
    throwIllegalInitializationUsage()
  }

  fun from(values: List<V1>) {
    throwIllegalInitializationUsage()
  }
}

@ConsistentCopyVisibility
data class DataVariable2<V1, V2> internal constructor(private val v1: V1, private val v2: V2) : DataVariable() {
  fun from(values1: List<V1>, values2: List<V2>) {
    throwIllegalInitializationUsage()
  }
}

@ConsistentCopyVisibility
data class DataVariable3<V1, V2, V3>
internal constructor(
  private val v1: V1,
  private val v2: V2,
  private val v3: V3
) : DataVariable() {
  fun from(values1: List<V1>, values2: List<V2>, values3: List<V3>) {
    throwIllegalInitializationUsage()
  }
}

@ConsistentCopyVisibility
data class DataVariable4<V1, V2, V3, V4>
internal constructor(
  private val v1: V1,
  private val v2: V2,
  private val v3: V3,
  private val v4: V4
) : DataVariable() {
  fun from(values1: List<V1>, values2: List<V2>, values3: List<V3>, values4: List<V4>) {
    throwIllegalInitializationUsage()
  }
}

@ConsistentCopyVisibility
data class DataVariable5<V1, V2, V3, V4, V5>
internal constructor(
  private val v1: V1,
  private val v2: V2,
  private val v3: V3,
  private val v4: V4,
  private val v5: V5
) : DataVariable() {
  fun from(
    values1: List<V1>,
    values2: List<V2>,
    values3: List<V3>,
    values4: List<V4>,
    values5: List<V5>
  ) {
    throwIllegalInitializationUsage()
  }
}

@ConsistentCopyVisibility
data class DataVariable6<V1, V2, V3, V4, V5, V6>
internal constructor(
  private val v1: V1,
  private val v2: V2,
  private val v3: V3,
  private val v4: V4,
  private val v5: V5,
  private val v6: V6
) : DataVariable() {
  fun from(
    values1: List<V1>,
    values2: List<V2>,
    values3: List<V3>,
    values4: List<V4>,
    values5: List<V5>,
    values6: List<V6>
  ) {
    throwIllegalInitializationUsage()
  }
}

@ConsistentCopyVisibility
data class DataVariable7<V1, V2, V3, V4, V5, V6, V7>
internal constructor(
  private val v1: V1,
  private val v2: V2,
  private val v3: V3,
  private val v4: V4,
  private val v5: V5,
  private val v6: V6,
  private val v7: V7
) : DataVariable() {
  fun from(
    values1: List<V1>,
    values2: List<V2>,
    values3: List<V3>,
    values4: List<V4>,
    values5: List<V5>,
    values6: List<V6>,
    values7: List<V7>
  ) {
    throwIllegalInitializationUsage()
  }
}

@ConsistentCopyVisibility
data class DataVariable8<V1, V2, V3, V4, V5, V6, V7, V8>
internal constructor(
  private val v1: V1,
  private val v2: V2,
  private val v3: V3,
  private val v4: V4,
  private val v5: V5,
  private val v6: V6,
  private val v7: V7,
  private val v8: V8
) : DataVariable() {
  fun from(
    values1: List<V1>,
    values2: List<V2>,
    values3: List<V3>,
    values4: List<V4>,
    values5: List<V5>,
    values6: List<V6>,
    values7: List<V7>,
    values8: List<V8>
  ) {
    throwIllegalInitializationUsage()
  }
}

@ConsistentCopyVisibility
data class DataVariable9<V1, V2, V3, V4, V5, V6, V7, V8, V9>
internal constructor(
  private val v1: V1,
  private val v2: V2,
  private val v3: V3,
  private val v4: V4,
  private val v5: V5,
  private val v6: V6,
  private val v7: V7,
  private val v8: V8,
  private val v9: V9
) : DataVariable() {
  fun from(
    values1: List<V1>,
    values2: List<V2>,
    values3: List<V3>,
    values4: List<V4>,
    values5: List<V5>,
    values6: List<V6>,
    values7: List<V7>,
    values8: List<V8>,
    values9: List<V9>
  ) {
    throwIllegalInitializationUsage()
  }
}

@ConsistentCopyVisibility
data class DataVariable10<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10>
internal constructor(
  private val v1: V1,
  private val v2: V2,
  private val v3: V3,
  private val v4: V4,
  private val v5: V5,
  private val v6: V6,
  private val v7: V7,
  private val v8: V8,
  private val v9: V9,
  private val v10: V10
) : DataVariable() {
  fun from(
    values1: List<V1>,
    values2: List<V2>,
    values3: List<V3>,
    values4: List<V4>,
    values5: List<V5>,
    values6: List<V6>,
    values7: List<V7>,
    values8: List<V8>,
    values9: List<V9>,
    values10: List<V10>
  ) {
    throwIllegalInitializationUsage()
  }
}
