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
import io.github.pshevche.spockk.lang.expect
import spock.lang.Specification
import java.lang.reflect.Proxy

class MockProxyCaching : Specification() {

  @MigratedFrom("org.spockframework.smoke.mock.MockProxyCaching#dynamic proxy classes are cached")
  fun `dynamic proxy classes are cached`() {
    val list1 = Mock(List::class.java)
    val list2 = Mock(List::class.java)

    expect
    Proxy.isProxyClass(list1.javaClass)
    Proxy.isProxyClass(list2.javaClass)
    list1.javaClass == list2.javaClass
  }

  @MigratedFrom("org.spockframework.smoke.mock.MockProxyCaching#Byte Buddy or CGLIB proxy classes are cached")
  fun `Byte Buddy or CGLIB proxy classes are cached`() {
    val list1 = Mock(ArrayList::class.java)
    val list2 = Mock(ArrayList::class.java)

    expect
    list1.javaClass == list2.javaClass
  }
}
