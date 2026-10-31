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
import io.github.pshevche.spockk.lang.Stub
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import spock.lang.Specification

class GroovyMocksForInterfaces : Specification() {

  interface Person {
    fun getName(): String

    fun setName(name: String)

    fun sing(song: String)
  }

  interface BooleanGetters {
    fun isBooleanValue(): Boolean

    fun isBooleanValueBoxed(): Boolean?

    fun getIsBooleanValue(): Boolean

    fun getIsBooleanValueBoxed(): Boolean?
  }

  private val person = Mock(Person::class.java)

  @MigratedFrom("org.spockframework.smoke.mock.GroovyMocksForInterfaces#physical method")
  fun `physical method`() {
    `when`
    person.sing("song")

    then
    1 * person.sing("song")
  }

  @MigratedFrom("org.spockframework.smoke.mock.GroovyMocksForInterfaces#get physical property")
  fun `get physical property`() {
    `when`
    person.getName()

    then
    1 * person.getName()

    `when`
    person.getName()

    then
    1 * person.getName()
  }

  @MigratedFrom("org.spockframework.smoke.mock.GroovyMocksForInterfaces#set physical property")
  fun `set physical property`() {
    `when`
    person.setName("fred")

    then
    1 * person.setName("fred")
  }

  @MigratedFrom("org.spockframework.smoke.mock.GroovyMocksForInterfaces#Boolean is getters shall be mockable")
  fun `Boolean is getters shall be mockable`() {
    val m =
      Stub(BooleanGetters::class.java) {
        isBooleanValue() returns true
      }

    expect
    m.isBooleanValue()
  }

  @MigratedFrom("org.spockframework.smoke.mock.GroovyMocksForInterfaces#Boolean get getters shall be mockable")
  fun `Boolean get getters shall be mockable`() {
    val m =
      Stub(BooleanGetters::class.java) {
        getIsBooleanValue() returns true
      }

    expect
    m.getIsBooleanValue()
  }

  @MigratedFrom("org.spockframework.smoke.mock.GroovyMocksForInterfaces#Boolean boxed get getters shall be mockable")
  fun `Boolean boxed get getters shall be mockable`() {
    val m =
      Stub(BooleanGetters::class.java) {
        getIsBooleanValueBoxed() returns true
      }

    expect
    m.getIsBooleanValueBoxed() == true
  }
}
