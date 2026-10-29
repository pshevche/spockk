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

package io.github.pshevche.spockk.fixtures.runtime.samples.extension

import groovy.lang.Closure
import io.github.pshevche.spockk.lang.expect
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.PendingFeatureIf
import spock.lang.Requires
import spock.lang.Specification

class InheritanceFixture(val inherited: Boolean, val classes: List<Class<*>>)

open class Base1 : Specification() {
  fun `base feature`() {
    expect
    true
  }
}

@Requires(FalseCondition::class, inherited = true)
open class Foo1 : Base1() {
  @Requires(UndeclaredVariableCondition::class)
  fun `foo feature`() {
    expect
    true
  }
}

open class Bar1 : Foo1() {
  @Requires(UndeclaredVariableCondition::class)
  fun `bar feature`() {
    expect
    true
  }
}

class Test1 : Bar1() {
  @Requires(UndeclaredVariableCondition::class)
  fun `test feature`() {
    expect
    true
  }
}

open class Base2 : Specification() {
  fun `base feature`() {
    expect
    true
  }
}

@Requires(FalseCondition::class, inherited = true)
open class Foo2 : Base2() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `foo feature`() {
    expect
    true
  }
}

open class Bar2 : Foo2() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `bar feature`() {
    expect
    true
  }
}

class Test2 : Bar2() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `test feature`() {
    expect
    true
  }
}

open class Base3 : Specification() {
  fun `base feature`() {
    expect
    true
  }
}

@Requires(FalseCondition::class, inherited = true)
open class Foo3 : Base3() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `foo feature`() {
    expect
    true
  }
}

open class Bar3 : Foo3() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `bar feature`() {
    expect
    true
  }
}

class Test3 : Bar3() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `test feature`() {
    expect
    true
  }
}

open class Base4 : Specification() {
  fun `base feature`() {
    expect
    true
  }
}

@Requires(FalseCondition::class, inherited = false)
open class Foo4 : Base4() {
  @Requires(UndeclaredVariableCondition::class)
  fun `foo feature`() {
    expect
    true
  }
}

open class Bar4 : Foo4() {
  @Requires(UndeclaredVariableCondition::class)
  fun `bar feature`() {
    expect
    true
  }
}

class Test4 : Bar4() {
  @Requires(UndeclaredVariableCondition::class)
  fun `test feature`() {
    expect
    true
  }
}

open class Base5 : Specification() {
  fun `base feature`() {
    expect
    true
  }
}

@Requires(FalseCondition::class, inherited = false)
open class Foo5 : Base5() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `foo feature`() {
    expect
    true
  }
}

open class Bar5 : Foo5() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `bar feature`() {
    expect
    true
  }
}

class Test5 : Bar5() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `test feature`() {
    expect
    true
  }
}

open class Base6 : Specification() {
  fun `base feature`() {
    expect
    true
  }
}

@Requires(FalseCondition::class, inherited = false)
open class Foo6 : Base6() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `foo feature`() {
    expect
    true
  }
}

open class Bar6 : Foo6() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `bar feature`() {
    expect
    true
  }
}

class Test6 : Bar6() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `test feature`() {
    expect
    true
  }
}

open class Base7 : Specification() {
  fun `base feature`() {
    expect
    true
  }
}

@IgnoreIf(TrueCondition::class, inherited = true)
open class Foo7 : Base7() {
  @Requires(UndeclaredVariableCondition::class)
  fun `foo feature`() {
    expect
    true
  }
}

open class Bar7 : Foo7() {
  @Requires(UndeclaredVariableCondition::class)
  fun `bar feature`() {
    expect
    true
  }
}

class Test7 : Bar7() {
  @Requires(UndeclaredVariableCondition::class)
  fun `test feature`() {
    expect
    true
  }
}

open class Base8 : Specification() {
  fun `base feature`() {
    expect
    true
  }
}

@IgnoreIf(TrueCondition::class, inherited = true)
open class Foo8 : Base8() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `foo feature`() {
    expect
    true
  }
}

open class Bar8 : Foo8() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `bar feature`() {
    expect
    true
  }
}

class Test8 : Bar8() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `test feature`() {
    expect
    true
  }
}

open class Base9 : Specification() {
  fun `base feature`() {
    expect
    true
  }
}

@IgnoreIf(TrueCondition::class, inherited = true)
open class Foo9 : Base9() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `foo feature`() {
    expect
    true
  }
}

open class Bar9 : Foo9() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `bar feature`() {
    expect
    true
  }
}

class Test9 : Bar9() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `test feature`() {
    expect
    true
  }
}

open class Base10 : Specification() {
  fun `base feature`() {
    expect
    true
  }
}

@IgnoreIf(TrueCondition::class, inherited = false)
open class Foo10 : Base10() {
  @Requires(UndeclaredVariableCondition::class)
  fun `foo feature`() {
    expect
    true
  }
}

open class Bar10 : Foo10() {
  @Requires(UndeclaredVariableCondition::class)
  fun `bar feature`() {
    expect
    true
  }
}

class Test10 : Bar10() {
  @Requires(UndeclaredVariableCondition::class)
  fun `test feature`() {
    expect
    true
  }
}

open class Base11 : Specification() {
  fun `base feature`() {
    expect
    true
  }
}

@IgnoreIf(TrueCondition::class, inherited = false)
open class Foo11 : Base11() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `foo feature`() {
    expect
    true
  }
}

open class Bar11 : Foo11() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `bar feature`() {
    expect
    true
  }
}

class Test11 : Bar11() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `test feature`() {
    expect
    true
  }
}

open class Base12 : Specification() {
  fun `base feature`() {
    expect
    true
  }
}

@IgnoreIf(TrueCondition::class, inherited = false)
open class Foo12 : Base12() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `foo feature`() {
    expect
    true
  }
}

open class Bar12 : Foo12() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `bar feature`() {
    expect
    true
  }
}

class Test12 : Bar12() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `test feature`() {
    expect
    true
  }
}

open class Base13 : Specification() {
  fun `base feature`() {
    expect
    true
  }
}

@Ignore(inherited = true)
open class Foo13 : Base13() {
  @Requires(UndeclaredVariableCondition::class)
  fun `foo feature`() {
    expect
    true
  }
}

open class Bar13 : Foo13() {
  @Requires(UndeclaredVariableCondition::class)
  fun `bar feature`() {
    expect
    true
  }
}

class Test13 : Bar13() {
  @Requires(UndeclaredVariableCondition::class)
  fun `test feature`() {
    expect
    true
  }
}

open class Base14 : Specification() {
  fun `base feature`() {
    expect
    true
  }
}

@Ignore(inherited = true)
open class Foo14 : Base14() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `foo feature`() {
    expect
    true
  }
}

open class Bar14 : Foo14() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `bar feature`() {
    expect
    true
  }
}

class Test14 : Bar14() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `test feature`() {
    expect
    true
  }
}

open class Base15 : Specification() {
  fun `base feature`() {
    expect
    true
  }
}

@Ignore(inherited = true)
open class Foo15 : Base15() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `foo feature`() {
    expect
    true
  }
}

open class Bar15 : Foo15() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `bar feature`() {
    expect
    true
  }
}

class Test15 : Bar15() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `test feature`() {
    expect
    true
  }
}

open class Base16 : Specification() {
  fun `base feature`() {
    expect
    true
  }
}

@Ignore(inherited = false)
open class Foo16 : Base16() {
  @Requires(UndeclaredVariableCondition::class)
  fun `foo feature`() {
    expect
    true
  }
}

open class Bar16 : Foo16() {
  @Requires(UndeclaredVariableCondition::class)
  fun `bar feature`() {
    expect
    true
  }
}

class Test16 : Bar16() {
  @Requires(UndeclaredVariableCondition::class)
  fun `test feature`() {
    expect
    true
  }
}

open class Base17 : Specification() {
  fun `base feature`() {
    expect
    true
  }
}

@Ignore(inherited = false)
open class Foo17 : Base17() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `foo feature`() {
    expect
    true
  }
}

open class Bar17 : Foo17() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `bar feature`() {
    expect
    true
  }
}

class Test17 : Bar17() {
  @IgnoreIf(UndeclaredVariableCondition::class)
  fun `test feature`() {
    expect
    true
  }
}

open class Base18 : Specification() {
  fun `base feature`() {
    expect
    true
  }
}

@Ignore(inherited = false)
open class Foo18 : Base18() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `foo feature`() {
    expect
    true
  }
}

open class Bar18 : Foo18() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `bar feature`() {
    expect
    true
  }
}

class Test18 : Bar18() {
  @PendingFeatureIf(UndeclaredVariableCondition::class)
  fun `test feature`() {
    expect
    true
  }
}

object InheritanceFixtures {
  val all: List<InheritanceFixture> = listOf(
    InheritanceFixture(true, listOf(Base1::class.java, Foo1::class.java, Bar1::class.java, Test1::class.java)),
    InheritanceFixture(true, listOf(Base2::class.java, Foo2::class.java, Bar2::class.java, Test2::class.java)),
    InheritanceFixture(true, listOf(Base3::class.java, Foo3::class.java, Bar3::class.java, Test3::class.java)),
    InheritanceFixture(false, listOf(Base4::class.java, Foo4::class.java, Bar4::class.java, Test4::class.java)),
    InheritanceFixture(false, listOf(Base5::class.java, Foo5::class.java, Bar5::class.java, Test5::class.java)),
    InheritanceFixture(false, listOf(Base6::class.java, Foo6::class.java, Bar6::class.java, Test6::class.java)),
    InheritanceFixture(true, listOf(Base7::class.java, Foo7::class.java, Bar7::class.java, Test7::class.java)),
    InheritanceFixture(true, listOf(Base8::class.java, Foo8::class.java, Bar8::class.java, Test8::class.java)),
    InheritanceFixture(true, listOf(Base9::class.java, Foo9::class.java, Bar9::class.java, Test9::class.java)),
    InheritanceFixture(false, listOf(Base10::class.java, Foo10::class.java, Bar10::class.java, Test10::class.java)),
    InheritanceFixture(false, listOf(Base11::class.java, Foo11::class.java, Bar11::class.java, Test11::class.java)),
    InheritanceFixture(false, listOf(Base12::class.java, Foo12::class.java, Bar12::class.java, Test12::class.java)),
    InheritanceFixture(true, listOf(Base13::class.java, Foo13::class.java, Bar13::class.java, Test13::class.java)),
    InheritanceFixture(true, listOf(Base14::class.java, Foo14::class.java, Bar14::class.java, Test14::class.java)),
    InheritanceFixture(true, listOf(Base15::class.java, Foo15::class.java, Bar15::class.java, Test15::class.java)),
    InheritanceFixture(false, listOf(Base16::class.java, Foo16::class.java, Bar16::class.java, Test16::class.java)),
    InheritanceFixture(false, listOf(Base17::class.java, Foo17::class.java, Bar17::class.java, Test17::class.java)),
    InheritanceFixture(false, listOf(Base18::class.java, Foo18::class.java, Bar18::class.java, Test18::class.java))
  )
}
