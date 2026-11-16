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

import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.verifyAll
import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.MethodInfo
import org.spockframework.runtime.model.SpecInfo
import spock.lang.Specification
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Retention(AnnotationRetention.RUNTIME)
@ExtensionAnnotation(FooExtension::class)
@Repeatable
@JvmRepeatable(FooContainer::class)
annotation class Foo

@Retention(AnnotationRetention.RUNTIME)
annotation class FooContainer(val value: Array<Foo>)

object FooVisits {
  private val counts = ConcurrentHashMap<Pair<Class<*>, String>, AtomicInteger>()

  fun record(spec: Class<*>, kind: String) {
    counts.computeIfAbsent(spec to kind) { AtomicInteger() }.incrementAndGet()
  }

  fun count(spec: Class<*>, kind: String): Int = counts[spec to kind]?.get() ?: 0
}

class FooExtension : IAnnotationDrivenExtension<Foo> {
  override fun visitSpecAnnotation(annotation: Foo, spec: SpecInfo) =
    FooVisits.record(spec.reflection, "spec")

  override fun visitFeatureAnnotation(annotation: Foo, feature: FeatureInfo) =
    FooVisits.record(feature.spec.reflection, "feature")

  override fun visitFixtureAnnotation(annotation: Foo, fixtureMethod: MethodInfo) =
    FooVisits.record(fixtureMethod.parent.reflection, "fixture")

  override fun visitFieldAnnotation(annotation: Foo, field: FieldInfo) =
    FooVisits.record(field.parent.reflection, "field")

  override fun visitSpec(spec: SpecInfo) {}
}

class RepeatedSpecAnnotations1Spec : Specification() {
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

@Foo
class RepeatedSpecAnnotations2Spec : Specification() {
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 1
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

@Foo @Foo
class RepeatedSpecAnnotations3Spec : Specification() {
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 2
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

@FooContainer([])
class RepeatedSpecAnnotations4Spec : Specification() {
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

@Foo
@FooContainer([])
class RepeatedSpecAnnotations5Spec : Specification() {
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 1
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

@FooContainer([Foo()])
class RepeatedSpecAnnotations6Spec : Specification() {
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 1
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

@FooContainer([Foo()])
class RepeatedSpecAnnotations7Spec : Specification() {
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 1
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

@Foo
@FooContainer([Foo()])
class RepeatedSpecAnnotations8Spec : Specification() {
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 2
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

@Foo
@FooContainer([Foo()])
class RepeatedSpecAnnotations9Spec : Specification() {
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 2
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

@FooContainer([Foo(), Foo()])
class RepeatedSpecAnnotations10Spec : Specification() {
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 2
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

@Foo
@FooContainer([Foo(), Foo()])
class RepeatedSpecAnnotations11Spec : Specification() {
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 3
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFeatureAnnotations1Spec : Specification() {

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFeatureAnnotations2Spec : Specification() {
  @Foo
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 1
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFeatureAnnotations3Spec : Specification() {
  @Foo @Foo
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 2
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFeatureAnnotations4Spec : Specification() {
  @FooContainer([])
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFeatureAnnotations5Spec : Specification() {
  @Foo
  @FooContainer([])
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 1
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFeatureAnnotations6Spec : Specification() {
  @FooContainer([Foo()])
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 1
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFeatureAnnotations7Spec : Specification() {
  @FooContainer([Foo()])
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 1
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFeatureAnnotations8Spec : Specification() {
  @Foo
  @FooContainer([Foo()])
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 2
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFeatureAnnotations9Spec : Specification() {
  @Foo
  @FooContainer([Foo()])
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 2
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFeatureAnnotations10Spec : Specification() {
  @FooContainer([Foo(), Foo()])
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 2
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFeatureAnnotations11Spec : Specification() {
  @Foo
  @FooContainer([Foo(), Foo()])
  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 3
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFixtureAnnotations1Spec : Specification() {

  fun setup() {}

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFixtureAnnotations2Spec : Specification() {
  @Foo
  fun setup() {}

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 1
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFixtureAnnotations3Spec : Specification() {
  @Foo @Foo
  fun setup() {}

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 2
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFixtureAnnotations4Spec : Specification() {
  @FooContainer([])
  fun setup() {}

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFixtureAnnotations5Spec : Specification() {
  @Foo
  @FooContainer([])
  fun setup() {}

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 1
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFixtureAnnotations6Spec : Specification() {
  @FooContainer([Foo()])
  fun setup() {}

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 1
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFixtureAnnotations7Spec : Specification() {
  @FooContainer([Foo()])
  fun setup() {}

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 1
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFixtureAnnotations8Spec : Specification() {
  @Foo
  @FooContainer([Foo()])
  fun setup() {}

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 2
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFixtureAnnotations9Spec : Specification() {
  @Foo
  @FooContainer([Foo()])
  fun setup() {}

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 2
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFixtureAnnotations10Spec : Specification() {
  @FooContainer([Foo(), Foo()])
  fun setup() {}

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 2
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFixtureAnnotations11Spec : Specification() {
  @Foo
  @FooContainer([Foo(), Foo()])
  fun setup() {}

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 3
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFieldAnnotations1Spec : Specification() {

  val foo: Any? = null

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFieldAnnotations2Spec : Specification() {
  @field:Foo
  val foo: Any? = null

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 1
    }
  }
}

class RepeatedFieldAnnotations3Spec : Specification() {
  @field:Foo @field:Foo
  val foo: Any? = null

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 2
    }
  }
}

class RepeatedFieldAnnotations4Spec : Specification() {
  @field:FooContainer([])
  val foo: Any? = null

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 0
    }
  }
}

class RepeatedFieldAnnotations5Spec : Specification() {
  @field:Foo
  @field:FooContainer([])
  val foo: Any? = null

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 1
    }
  }
}

class RepeatedFieldAnnotations6Spec : Specification() {
  @field:FooContainer([Foo()])
  val foo: Any? = null

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 1
    }
  }
}

class RepeatedFieldAnnotations7Spec : Specification() {
  @field:FooContainer([Foo()])
  val foo: Any? = null

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 1
    }
  }
}

class RepeatedFieldAnnotations8Spec : Specification() {
  @field:Foo
  @field:FooContainer([Foo()])
  val foo: Any? = null

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 2
    }
  }
}

class RepeatedFieldAnnotations9Spec : Specification() {
  @field:Foo
  @field:FooContainer([Foo()])
  val foo: Any? = null

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 2
    }
  }
}

class RepeatedFieldAnnotations10Spec : Specification() {
  @field:FooContainer([Foo(), Foo()])
  val foo: Any? = null

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 2
    }
  }
}

class RepeatedFieldAnnotations11Spec : Specification() {
  @field:Foo
  @field:FooContainer([Foo(), Foo()])
  val foo: Any? = null

  fun foo() {
    expect
    verifyAll {
      FooVisits.count(javaClass, "spec") == 0
      FooVisits.count(javaClass, "feature") == 0
      FooVisits.count(javaClass, "fixture") == 0
      FooVisits.count(javaClass, "field") == 3
    }
  }
}
