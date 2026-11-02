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
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import spock.lang.Specification
import java.util.stream.DoubleStream
import java.util.stream.IntStream
import java.util.stream.LongStream
import java.util.stream.Stream

class StreamSpec : Specification() {

  @MigratedFrom("org.spockframework.smoke.mock.StreamSpec#Streams are empty")
  fun `Streams are empty`() {
    given
    val service: StreamService = Stub(StreamService::class.java)

    expect
    service.stringStream().count() == 0L
    service.intStream().count() == 0L
    service.doubleStream().count() == 0L
    service.longStream().count() == 0L
  }

  interface StreamService {
    fun stringStream(): Stream<String>

    fun intStream(): IntStream

    fun doubleStream(): DoubleStream

    fun longStream(): LongStream
  }
}
