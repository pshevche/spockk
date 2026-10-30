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
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.variables
import io.github.pshevche.spockk.lang.where
import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.extension.builtin.RetryConditionContext
import org.spockframework.runtime.extension.builtin.RetryIterationInterceptor
import org.spockframework.runtime.model.FeatureInfo
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

object RetryTracker {
  val setupCounter = AtomicInteger()
  val cleanupCounter = AtomicInteger()
  val featureCounter = AtomicInteger()
  val extensionCounter = AtomicInteger()
  val sequenceCounter = AtomicInteger()
  var iterationBuffer = StringBuffer()

  @Volatile var staticValue = 0

  fun reset() {
    setupCounter.set(0)
    cleanupCounter.set(0)
    featureCounter.set(0)
    extensionCounter.set(0)
    sequenceCounter.set(0)
    iterationBuffer = StringBuffer()
    staticValue = 0
  }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@ExtensionAnnotation(ChangeThreadExtension::class)
annotation class ChangeThread

class ChangeThreadExtension : IAnnotationDrivenExtension<ChangeThread> {
  override fun visitFeatureAnnotation(annotation: ChangeThread, feature: FeatureInfo) {
    check(feature.iterationInterceptors.any { it is RetryIterationInterceptor }) {
      "RetryIterationInterceptor must be added first"
    }
    feature.addIterationInterceptor { invocation ->
      Thread { invocation.proceed() }.apply {
        start()
        join()
      }
    }
  }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@ExtensionAnnotation(CountExecutionExtension::class)
annotation class CountExecution

class CountExecutionExtension : IAnnotationDrivenExtension<CountExecution> {
  override fun visitFeatureAnnotation(annotation: CountExecution, feature: FeatureInfo) {
    feature.featureMethod.addInterceptor { invocation ->
      RetryTracker.extensionCounter.incrementAndGet()
      invocation.resolveArgument(0, "BAZ")
      invocation.proceed()
    }
  }
}

class RetryExhaustedFixture : Specification() {
  @Retry
  fun bar() {
    RetryTracker.featureCounter.incrementAndGet()
    expect
    false
  }
}

class RetryIoExceptionFixture : Specification() {
  @Retry
  fun bar() {
    RetryTracker.featureCounter.incrementAndGet()
    expect
    throw IOException()
  }
}

class RetryIterationModeFixture : Specification() {
  fun setup() {
    RetryTracker.setupCounter.incrementAndGet()
  }

  @Retry(mode = Retry.Mode.ITERATION)
  fun bar() {
    RetryTracker.featureCounter.incrementAndGet()
    expect
    throw IOException()
  }

  fun cleanup() {
    RetryTracker.cleanupCounter.incrementAndGet()
  }
}

class RetrySetupFeatureCleanupModeFixture : Specification() {
  fun setup() {
    RetryTracker.setupCounter.incrementAndGet()
  }

  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  fun bar() {
    RetryTracker.featureCounter.incrementAndGet()
    expect
    throw IOException()
  }

  fun cleanup() {
    RetryTracker.cleanupCounter.incrementAndGet()
  }
}

class RetryChangeThreadFixture : Specification() {
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  @ChangeThread
  fun foo() {
    expect
    false
  }
}

class RetryChangeThreadDataDrivenFixture : Specification() {
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  @ChangeThread
  fun foo(i: Int) {
    expect
    false

    where
    variable(i).from(1, 2)
  }
}

class RetryIterationModeDataDrivenFixture : Specification() {
  fun setup() {
    RetryTracker.setupCounter.incrementAndGet()
  }

  @Retry(mode = Retry.Mode.ITERATION)
  fun bar(foo: Int) {
    RetryTracker.featureCounter.incrementAndGet()
    expect
    throw IOException()

    where
    variable(foo).from(1, 2, 3)
  }

  fun cleanup() {
    RetryTracker.cleanupCounter.incrementAndGet()
  }
}

class RetrySetupFeatureCleanupModeDataDrivenFixture : Specification() {
  fun setup() {
    RetryTracker.setupCounter.incrementAndGet()
  }

  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  fun bar(foo: Int) {
    RetryTracker.featureCounter.incrementAndGet()
    expect
    throw IOException()

    where
    variable(foo).from(1, 2, 3)
  }

  fun cleanup() {
    RetryTracker.cleanupCounter.incrementAndGet()
  }
}

class RetryCountFixture : Specification() {
  @Retry(count = 5)
  fun bar() {
    RetryTracker.featureCounter.incrementAndGet()
    expect
    throw Exception()
  }
}

class RetryUnhandledExceptionFixture : Specification() {
  @Retry(exceptions = [IndexOutOfBoundsException::class])
  fun bar() {
    expect
    throw IllegalArgumentException()
  }
}

class RetryUnhandledExceptionIterationModeFixture : Specification() {
  @Retry(exceptions = [IndexOutOfBoundsException::class], mode = Retry.Mode.ITERATION)
  fun bar(ignore: Int) {
    expect
    throw IllegalArgumentException()

    where
    variable(ignore).from(1, 2)
  }
}

class RetryUnhandledExceptionSetupFeatureCleanupModeFixture : Specification() {
  @Retry(exceptions = [IndexOutOfBoundsException::class], mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  fun bar(ignore: Int) {
    expect
    throw IllegalArgumentException()

    where
    variable(ignore).from(1, 2)
  }
}

class RetryDataDrivenAllFailingFixture : Specification() {
  @Retry
  fun bar(test: Boolean) {
    RetryTracker.featureCounter.incrementAndGet()
    expect
    test

    where
    variable(test).from(false, false)
  }
}

class RetryDataDrivenOneFailingFixture : Specification() {
  @Retry
  fun bar(test: Boolean) {
    RetryTracker.featureCounter.incrementAndGet()
    expect
    test

    where
    variable(test).from(false, true, true)
  }
}

class RetryDataDrivenAllPassingFixture : Specification() {
  @Retry
  fun bar(test: Boolean) {
    expect
    test

    where
    variable(test).from(true, true, true)
  }
}

class RetryIgnoresPreviousFailuresFixture : Specification() {
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  fun bar() {
    expect
    RetryTracker.sequenceCounter.getAndIncrement() > 0
  }
}

class RetryDelayFixture : Specification() {
  @Retry(delay = 100)
  fun bar(test: Boolean) {
    RetryTracker.featureCounter.incrementAndGet()
    expect
    test

    where
    variable(test).from(false, true, true)
  }
}

class FailureMessageContainsOneCondition(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  @Suppress("unused") // invoked reflectively by Groovy's Closure.call() dispatch
  fun doCall(): Boolean = (delegate as RetryConditionContext<*>).failure.message!!.contains("1")
}

class RetryConditionFixture : Specification() {
  @Retry(condition = FailureMessageContainsOneCondition::class)
  fun bar(message: String) {
    RetryTracker.featureCounter.incrementAndGet()
    expect
    assert(false) { message }

    where
    variable(message).from("1", "2", "3")
  }
}

class RetryUnexpectedExceptionFixture : Specification() {
  @Retry(exceptions = [IllegalArgumentException::class], condition = FailureMessageContainsOneCondition::class)
  fun bar(exceptionClass: Class<out Throwable>, message: String) {
    RetryTracker.featureCounter.incrementAndGet()
    expect
    throw exceptionClass.getConstructor(String::class.java).newInstance(message)

    where
    variables(exceptionClass, message).from(
      listOf(
        IllegalArgumentException::class.java,
        IllegalArgumentException::class.java,
        IllegalStateException::class.java,
        IllegalStateException::class.java,
        RuntimeException::class.java,
        RuntimeException::class.java
      ),
      listOf("1", "2", "1", "2", "1", "2")
    )
  }
}

class InstanceSharedValueIsTwoCondition(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  @Suppress("unused") // invoked reflectively by Groovy's Closure.call() dispatch
  fun doCall(): Boolean = (delegate as RetryConditionContext<*>).let { (it.instance as RetrySharedFieldFixture).value == 2 }
}

class RetrySharedFieldFixture : Specification() {
  @Shared var value = 0

  @Retry(condition = InstanceSharedValueIsTwoCondition::class)
  fun bar(input: Int) {
    RetryTracker.featureCounter.incrementAndGet()
    value = input
    expect
    false

    where
    variable(input).from(1, 2, 3)
  }
}

class InstanceValueIsTwoCondition(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  @Suppress("unused") // invoked reflectively by Groovy's Closure.call() dispatch
  fun doCall(): Boolean = (delegate as RetryConditionContext<*>).let { (it.instance as RetryInstanceFieldFixture).value == 2 }
}

class RetryInstanceFieldFixture : Specification() {
  var value = 0

  @Retry(condition = InstanceValueIsTwoCondition::class)
  fun bar(input: Int) {
    RetryTracker.featureCounter.incrementAndGet()
    value = input
    expect
    false

    where
    variable(input).from(1, 2, 3)
  }
}

class StaticValueIsTwoCondition(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  @Suppress("unused") // invoked reflectively by Groovy's Closure.call() dispatch
  fun doCall(): Boolean = RetryTracker.staticValue == 2
}

class RetryStaticFieldFixture : Specification() {
  @Retry(condition = StaticValueIsTwoCondition::class)
  fun bar(input: Int) {
    RetryTracker.featureCounter.incrementAndGet()
    RetryTracker.staticValue = input
    expect
    false

    where
    variable(input).from(1, 2, 3)
  }
}

@Retry
class RetryOnClassFixture : Specification() {
  fun foo() {
    RetryTracker.featureCounter.incrementAndGet()
    expect
    false
  }

  fun bar() {
    RetryTracker.featureCounter.incrementAndGet()
    expect
    true
  }

  @Retry(count = 1)
  fun baz() {
    RetryTracker.featureCounter.incrementAndGet()
    expect
    false
  }
}

@Retry(count = 1)
abstract class RetryOnAbstractBaseFixture : Specification()

class RetryInheritedFromBaseFixture : RetryOnAbstractBaseFixture() {
  fun bar() {
    RetryTracker.featureCounter.incrementAndGet()
    expect
    false
  }
}

abstract class RetryFreeBaseFixture : Specification() {
  fun foo() {
    RetryTracker.featureCounter.incrementAndGet()
    expect
    false
  }
}

@Retry(count = 1)
class RetryOnSubclassFixture : RetryFreeBaseFixture() {
  fun bar() {
    RetryTracker.featureCounter.incrementAndGet()
    expect
    false
  }
}

@Retry(count = 1)
abstract class RetryOverriddenBaseFixture : Specification() {
  fun foo() {
    RetryTracker.featureCounter.incrementAndGet()
    expect
    false
  }
}

@Retry(count = 2)
class RetryOverridingSubclassFixture : RetryOverriddenBaseFixture() {
  fun bar() {
    RetryTracker.featureCounter.incrementAndGet()
    expect
    false
  }
}

class RetryRemainingIterationsFixture : Specification() {
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  fun bar(iteration: Int) {
    RetryTracker.iterationBuffer.append(iteration)
    expect
    if (iteration == 2) {
      throw RuntimeException()
    }
    true

    where
    variable(iteration).from(1, 2, 3, 4)
  }
}

class RetryAllIterationsFailingFixture : Specification() {
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  fun bar(iteration: Int) {
    RetryTracker.iterationBuffer.append(iteration)
    expect
    false

    where
    variable(iteration).from(1, 2, 3)
  }
}

class RetryReportedIterationsFixture : Specification() {
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  fun bar(result: Boolean) {
    expect
    result

    where
    variable(result).from(true, false)
  }
}

class RetryInterceptorChainFixture : Specification() {
  @Retry
  @CountExecution
  fun bar(baz: String) {
    expect
    false
  }
}
