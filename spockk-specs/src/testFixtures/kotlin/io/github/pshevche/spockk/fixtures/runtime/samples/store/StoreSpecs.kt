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

package io.github.pshevche.spockk.fixtures.runtime.samples.store

import io.github.pshevche.spockk.lang.expect
import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.extension.IStore
import org.spockframework.runtime.model.MethodKind
import org.spockframework.runtime.model.SpecInfo
import org.spockframework.util.Assert
import spock.lang.Specification

object StoreRecorder {
  @Volatile var cleanupOrder: MutableList<MethodKind> = mutableListOf()

  @Volatile var failingKinds: Set<MethodKind> = emptySet()
}

@Retention(AnnotationRetention.RUNTIME)
@ExtensionAnnotation(FailingCleanupStoreExtension::class)
annotation class FailingStoreUsage

private fun SpecInfo.interceptEverywhere(interceptor: IMethodInterceptor) {
  val spec = bottomSpec
  spec.addSharedInitializerInterceptor(interceptor)
  spec.allSharedInitializerMethods.forEach { it.addInterceptor(interceptor) }
  spec.addInterceptor(interceptor)
  spec.addSetupSpecInterceptor(interceptor)
  spec.addInitializerInterceptor(interceptor)
  spec.addSetupInterceptor(interceptor)
  spec.allFixtureMethods.forEach { it.addInterceptor(interceptor) }
  spec.allFeatures.forEach {
    it.addInterceptor(interceptor)
    it.addIterationInterceptor(interceptor)
    it.featureMethod.addInterceptor(interceptor)
  }
  spec.addCleanupInterceptor(interceptor)
  spec.addCleanupSpecInterceptor(interceptor)
}

class FailingCleanupStoreExtension : IAnnotationDrivenExtension<FailingStoreUsage> {
  override fun visitSpecAnnotation(annotation: FailingStoreUsage, spec: SpecInfo) {
    spec.interceptEverywhere(FailingCleanupStoreInterceptor())
  }
}

class FailingCleanupStoreInterceptor : IMethodInterceptor {
  companion object {
    val NAMESPACE: IStore.Namespace = IStore.Namespace.create(FailingCleanupStoreInterceptor::class.java)
  }

  override fun intercept(invocation: IMethodInvocation) {
    val kind = invocation.method.kind
    invocation
      .getStore(NAMESPACE)
      .put<FailingValue>(
        kind,
        FailingValue(
          kind,
          "Failing at $kind - ${invocation.method.name}",
          StoreRecorder.failingKinds.contains(kind)
        )
      )
    invocation.proceed()
  }
}

class FailingValue(private val methodKind: MethodKind, private val value: String, private val failAtCleanup: Boolean) : AutoCloseable {
  override fun close() {
    StoreRecorder.cleanupOrder.add(methodKind)
    Assert.that(!failAtCleanup, value)
  }
}

@FailingStoreUsage
class FailingStoreSpec : Specification() {
  fun `a feature`() {
    expect
    true
  }
}
