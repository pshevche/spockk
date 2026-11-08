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
import io.github.pshevche.spockk.lang.Stub
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.runtime.extension.IDefaultValueProviderExtension
import spock.lang.Specification
import java.lang.reflect.Type

sealed interface IMaybe<T> {
  class Some<T>(val value: T) : IMaybe<T>

  class None<T> : IMaybe<T>

  companion object {
    fun <T> none(): IMaybe<T> = None()
  }
}

sealed interface IEither<L, R> {
  class Left<L, R>(val value: L) : IEither<L, R>

  class Right<L, R>(val value: R) : IEither<L, R>
}

class FinalClass(val value: String)

class MaybeDefaultValueProvider : IDefaultValueProviderExtension {
  override fun provideDefaultValue(type: Class<*>, exactType: Type): Any? = if (type == IMaybe::class.java) IMaybe.none<Any?>() else null
}

class FinalClassDefaultValueProvider : IDefaultValueProviderExtension {
  override fun provideDefaultValue(type: Class<*>, exactType: Type): Any? = if (type == FinalClass::class.java) FinalClass("default") else null
}

interface StubHolder {
  fun getMaybe(): IMaybe<String>

  fun getEither(): IEither<String, Exception>

  fun getFinal(): FinalClass
}

class DefaultValueProvider : Specification() {

  @MigratedFrom("org.spockframework.smoke.mock.DefaultValueProvider#Stub's EmptyOrDummyResponse does not support sealed interfaces by default")
  fun `Stub's EmptyOrDummyResponse does not support sealed interfaces by default`() {
    given
    val holder = Stub(StubHolder::class.java)

    `when`
    holder.getEither()

    then
    val e = thrown(IllegalArgumentException::class.java)
    e.message == "io.github.pshevche.spockk.smoke.mock.IEither is a sealed interface"
  }

  @MigratedFrom("org.spockframework.smoke.mock.DefaultValueProvider#Stub's EmptyOrDummyResponse can be extended via IDefaultValueProviderExtension")
  fun `Stub's EmptyOrDummyResponse can be extended via IDefaultValueProviderExtension`() {
    given
    val holder = Stub(StubHolder::class.java)

    `when`
    val maybe = holder.getMaybe()

    then
    maybe is IMaybe.None<*>
  }

  @MigratedFrom("org.spockframework.smoke.mock.DefaultValueProvider#Stub's EmptyOrDummyResponse can be extended via IDefaultValueProviderExtension for final classes")
  fun `Stub's EmptyOrDummyResponse can be extended via IDefaultValueProviderExtension for final classes`() {
    given
    val holder = Stub(StubHolder::class.java)

    `when`
    val finalClass = holder.getFinal()

    then
    finalClass is FinalClass
    finalClass.value == "default"
  }
}
