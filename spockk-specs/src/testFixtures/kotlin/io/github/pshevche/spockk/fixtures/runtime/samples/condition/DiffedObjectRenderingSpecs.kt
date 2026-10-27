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

package io.github.pshevche.spockk.fixtures.runtime.samples.condition

import io.github.pshevche.spockk.lang.expect
import spock.lang.Specification
import java.net.SocketTimeoutException

class IntegerEqualsNullSpec : Specification() {
  fun `test`() {
    val one: Any = 1
    expect
    one == null
  }
}

class MyBeanDiffSpec : Specification() {
  fun `test`() {
    val b1 = MyBean().apply {
      integer = 1
      string = "fun"
    }
    val b2 = MyBean().apply {
      integer = 2
      string = "fun2"
    }
    expect
    b1 == b2
  }
}

class MySubBeanDiffSpec : Specification() {
  fun `test`() {
    val b1 = MySubBean().apply {
      integer = 1
      string = "fun"
    }
    val b2 = MySubBean().apply {
      integer = 2
      string = "fun2"
    }
    expect
    b1 == b2
  }
}

class MyBean2DiffSpec : Specification() {
  fun `test`() {
    val b1 = MyBean2().apply {
      setLong(1)
      string = "fun"
    }
    val b2 = MyBean2().apply {
      setLong(2)
      string = "fun2"
    }
    expect
    b1 == b2
  }
}

class MySubBean2DiffSpec : Specification() {
  fun `test`() {
    val b1 = MySubBean2().apply {
      setLong(1)
      string = "fun"
    }
    val b2 = MySubBean2().apply {
      setLong(2)
      string = "fun2"
    }
    expect
    b1 == b2
  }
}

class DifferentClassesDiffSpec : Specification() {
  fun `test`() {
    expect
    SocketTimeoutException().javaClass == ClassNotFoundException().javaClass
  }
}

class SameNamedClassesDiffSpec : Specification() {
  fun `test`() {
    val clazz1 = IsolatingClassLoader().loadClass(IsolatedClazz::class.java.name)
    val clazz2 = IsolatingClassLoader().loadClass(IsolatedClazz::class.java.name)
    expect
    clazz1 == clazz2
  }
}

interface RenderBean {
  fun getLong(): Long
}

open class MyBean {
  var integer: Int = 0
  var string: String? = null

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    other as MyBean
    return integer == other.integer && string == other.string
  }

  override fun hashCode(): Int = 31 * integer + string.hashCode()
}

open class MyBean2 : RenderBean {
  private var long1: Long = 0
  var string: String? = null

  override fun getLong(): Long = long1

  fun setLong(long1: Long) {
    this.long1 = long1
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    other as MyBean2
    return long1 == other.long1 && string == other.string
  }

  override fun hashCode(): Int = 31 * long1.hashCode() + string.hashCode()
}

class MySubBean : MyBean()

class MySubBean2 : MyBean2()

class IsolatedClazz

// Defines IsolatedClazz itself, so each instance yields a distinct class with the same name
class IsolatingClassLoader : ClassLoader(IsolatingClassLoader::class.java.classLoader) {
  override fun loadClass(name: String, resolve: Boolean): Class<*> {
    if (name != IsolatedClazz::class.java.name) return super.loadClass(name, resolve)
    val bytes = parent.getResourceAsStream(name.replace('.', '/') + ".class")!!.use { it.readBytes() }
    return defineClass(name, bytes, 0, bytes.size)
  }
}
