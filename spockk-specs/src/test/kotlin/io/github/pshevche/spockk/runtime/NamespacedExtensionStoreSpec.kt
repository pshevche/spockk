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

package io.github.pshevche.spockk.runtime

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.and
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.runtime.StoreProvider
import org.spockframework.runtime.extension.IStore
import spock.lang.AutoCleanup
import spock.lang.Specification
import java.util.function.Function
import java.util.function.Supplier

private val NS1 = IStore.Namespace.create("NS1")
private val NS2 = IStore.Namespace.create("NS2")

@Suppress("UNCHECKED_CAST")
private val nullableStringType = String::class.java as Class<String?>

private val defaultSupplier = Supplier { "default" }
private val nullSupplier = Supplier<String?> { null }
private val failingSupplier = Supplier<String> { throw RuntimeException() }
private val nullCreator = Function<String, String?> { null }
private val failingCreator = Function<String, String> { throw RuntimeException() }
private val failingNullableCreator = Function<String, String?> { throw RuntimeException() }

class NamespacedExtensionStoreSpec : Specification() {

  @field:AutoCleanup
  private val rootStoreProvider = StoreProvider.createRootStoreProvider()

  @field:AutoCleanup
  private val childStoreProvider = rootStoreProvider.createChildStoreProvider()

  private val ns1Root: IStore = rootStoreProvider.getStore(NS1)
  private val ns1Child: IStore = childStoreProvider.getStore(NS1)

  @MigratedFrom("org.spockframework.runtime.NamespacedExtensionStoreSpec#can put something in and retrieve it")
  fun `can put something in and retrieve it`() {
    `when`
    ns1Root.put<Any?>("key", "something")

    then
    ns1Root.get<String>("key") == "something"
    ns1Root.get("key", String::class.java) == "something"
  }

  @MigratedFrom("org.spockframework.runtime.NamespacedExtensionStoreSpec#can put something in root and retrieve it in the child")
  fun `can put something in root and retrieve it in the child`() {
    `when`
    ns1Root.put<Any?>("key", "something")

    then
    ns1Child.get<String>("key") == "something"
    ns1Child.get("key", String::class.java) == "something"
  }

  @MigratedFrom("org.spockframework.runtime.NamespacedExtensionStoreSpec#child values shadow parent values")
  fun `child values shadow parent values`() {
    `when`
    ns1Root.put<Any?>("key", "something")
    ns1Child.put<Any?>("key", "other thing")

    then
    ns1Root.get<String>("key") == "something"
    ns1Child.get("key", String::class.java) == "other thing"

    `when`
    ns1Child.remove<Any?>("key")

    then
    ns1Child.get<String>("key") == "something"
  }

  @MigratedFrom("org.spockframework.runtime.NamespacedExtensionStoreSpec#stores with different namespaces don't leak into each other")
  fun `stores with different namespaces don't leak into each other`() {
    given
    ns1Root.put<Any?>("key", "something")

    and
    val ns2Store: IStore = rootStoreProvider.getStore(NS2)

    and
    val ns1Sub: IStore = rootStoreProvider.getStore(NS1.append("sub"))

    expect
    ns2Store.get<String>("key") == null
    ns1Sub.get<String>("key") == null
  }

  @MigratedFrom("org.spockframework.runtime.NamespacedExtensionStoreSpec#returns default value if absent")
  fun `returns default value if absent`() {
    expect
    ns1Root.getOrDefault("key", String::class.java, "default") == "default"

    and
    ns1Root.get<String>("key") == null
  }

  @MigratedFrom("org.spockframework.runtime.NamespacedExtensionStoreSpec#returns default value from supplier if absent")
  fun `returns default value from supplier if absent`() {
    expect
    ns1Root.getOrDefault("key", String::class.java, defaultSupplier) == "default"

    and
    ns1Root.get<String>("key") == null
  }

  @MigratedFrom("org.spockframework.runtime.NamespacedExtensionStoreSpec#default value supplier is not called if value is present")
  fun `default value supplier is not called if value is present`() {
    given
    ns1Root.put<Any?>("key", "something")

    expect
    ns1Root.getOrDefault("key", String::class.java, failingSupplier) == "something"
    ns1Child.getOrDefault("key", String::class.java, failingSupplier) == "something"
  }

  @MigratedFrom("org.spockframework.runtime.NamespacedExtensionStoreSpec#retrieving a stored value with the wrong required type throws a StoreException")
  fun `retrieving a stored value with the wrong required type throws a StoreException`() {
    `when`
    ns1Root.get("nonexistent", String::class.java)

    then
    noExceptionThrown()

    `when`
    ns1Root.put<Any?>("key", emptyList<Any>())
    ns1Root.get("key", String::class.java)

    then
    thrown(IStore.StoreException::class.java)
  }

  @MigratedFrom("org.spockframework.runtime.NamespacedExtensionStoreSpec#removing a stored value with the wrong required type throws a StoreException")
  fun `removing a stored value with the wrong required type throws a StoreException`() {
    `when`
    ns1Root.remove("nonexistent", String::class.java)

    then
    noExceptionThrown()

    `when`
    ns1Root.put<Any?>("key", emptyList<Any>())
    ns1Root.remove("key", String::class.java)

    then
    thrown(IStore.StoreException::class.java)
  }

  @MigratedFrom("org.spockframework.runtime.NamespacedExtensionStoreSpec#using getOrDefault on a stored value with the wrong required type throws a StoreException")
  fun `using getOrDefault on a stored value with the wrong required type throws a StoreException`() {
    `when`
    ns1Root.put<Any?>("key", emptyList<Any>())
    ns1Root.getOrDefault("key", String::class.java, "")

    then
    thrown(IStore.StoreException::class.java)
  }

  @MigratedFrom("org.spockframework.runtime.NamespacedExtensionStoreSpec#using getOrComputeIfAbsent on a stored value with the wrong required type throws a StoreException")
  fun `using getOrComputeIfAbsent on a stored value with the wrong required type throws a StoreException`() {
    `when`
    ns1Root.put<Any?>("key", emptyList<Any>())
    ns1Root.getOrComputeIfAbsent("key", failingCreator, String::class.java)

    then
    thrown(IStore.StoreException::class.java)
  }

  @MigratedFrom("org.spockframework.runtime.NamespacedExtensionStoreSpec#returning null default values is valid")
  fun `returning null default values is valid`() {
    expect
    ns1Root.getOrDefault("key", nullableStringType, null as String?) == null
    ns1Root.getOrDefault("key", nullableStringType, nullSupplier) == null
    ns1Root.getOrComputeIfAbsent("a", nullCreator) == null
    ns1Root.getOrComputeIfAbsent("b", nullCreator, nullableStringType) == null
  }

  @MigratedFrom("org.spockframework.runtime.NamespacedExtensionStoreSpec#null values still count as present")
  fun `null values still count as present`() {
    given
    ns1Root.put<Any?>("key", null)

    expect
    ns1Root.getOrComputeIfAbsent("a", nullCreator) == null
    ns1Root.getOrComputeIfAbsent("a", failingNullableCreator) == null
    ns1Root.getOrComputeIfAbsent("key", failingNullableCreator) == null
  }

  @MigratedFrom("org.spockframework.runtime.NamespacedExtensionStoreSpec#root stores have no parent and return themselves as root")
  fun `root stores have no parent and return themselves as root`() {
    expect
    ns1Root.parentStore == null
    ns1Root.rootStore == ns1Root
  }

  @MigratedFrom("org.spockframework.runtime.NamespacedExtensionStoreSpec#child stores can navigate to the parent and root store")
  fun `child stores can navigate to the parent and root store`() {
    given
    val grandChildStoreProvider = childStoreProvider.createChildStoreProvider()
    val ns1GrandChild: IStore = grandChildStoreProvider.getStore(NS1)
    ns1Root.put<Any?>("key", "something")
    ns1Child.put<Any?>("key", "other thing")
    ns1GrandChild.put<Any?>("key", "grand thing")

    expect
    ns1Root.get<String>("key") == "something"

    and
    ns1Child.get<String>("key") == "other thing"
    ns1Child.parentStore!!.get<String>("key") == "something"
    ns1Child.rootStore.get<String>("key") == "something"

    and
    ns1GrandChild.get<String>("key") == "grand thing"
    ns1GrandChild.parentStore!!.get<String>("key") == "other thing"
    ns1GrandChild.rootStore.get<String>("key") == "something"
  }

  @MigratedFrom("org.spockframework.runtime.NamespacedExtensionStoreSpec#namespaces have readable toString")
  fun `namespaces have readable toString`() {
    expect
    NS1.toString() == "Namespace{parts=[NS1]}"
  }
}
