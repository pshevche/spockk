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

package io.github.pshevche.spockk.fixtures.runtime.samples.interceptors

import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.MethodInfo
import org.spockframework.runtime.model.SpecInfo

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtensionAnnotation(LifecycleRecorderAndContextTesterExtension::class)
annotation class LifecycleTest

/** Records the order of every interceptor call and checks the invocation context at each interception point. */
class LifecycleRecorderAndContextTesterExtension : IAnnotationDrivenExtension<LifecycleTest> {
  companion object {
    @Volatile
    var lifecycleOutline = ""
  }

  private var indent = ""

  override fun visitSpecAnnotation(annotation: LifecycleTest, spec: SpecInfo) {
    val allFeatures = spec.allFeatures
    val specsBottomToTop = spec.specsBottomToTop

    specsBottomToTop.forEach { s ->
      s.addSharedInitializerInterceptor { inv ->
        around(inv, "shared initializer", inv.spec.name, ::checkSpecContext)
      }
    }
    spec.allSharedInitializerMethods.forEach { m ->
      m.addInterceptor { inv ->
        around(inv, "shared initializer method", "${inv.spec.name}.${inv.method.name}()", ::checkSpecMethodContext)
      }
    }
    spec.addInterceptor { inv -> around(inv, "specification", inv.spec.name, ::checkSpecContext) }
    specsBottomToTop.forEach { s ->
      s.addSetupSpecInterceptor { inv -> around(inv, "setup spec", inv.spec.name, ::checkSpecContext) }
    }
    spec.allSetupSpecMethods.forEach { m ->
      m.addInterceptor { inv ->
        around(inv, "setup spec method", "${inv.spec.name}.${inv.method.name}()", ::checkSpecMethodContext)
      }
    }
    allFeatures.forEach { f ->
      f.addInterceptor { inv ->
        around(inv, "feature", "${inv.spec.name}.${inv.feature.name}", ::checkFeatureContext)
      }
    }
    specsBottomToTop.forEach { s ->
      s.addInitializerInterceptor { inv ->
        around(inv, "initializer", "${iterationName(inv)} / ${s.name}", ::checkIterationContext)
      }
    }
    spec.allInitializerMethods.forEach { m ->
      m.addInterceptor { inv ->
        around(inv, "initializer method", "${iterationMethodName(inv)}", ::checkIterationMethodContext)
      }
    }
    allFeatures.forEach { feature ->
      scopedMethods(feature, spec.allInitializerMethods, "feature scoped initializer method")
      scopedMethods(feature, spec.allSetupMethods, "feature scoped setup method")
      scopedMethods(feature, spec.allCleanupMethods, "feature scoped cleanup method")
    }
    allFeatures.forEach { f ->
      f.addInitializerInterceptor { inv ->
        around(inv, "feature scoped initializer", iterationName(inv), ::checkIterationContext)
      }
      f.addIterationInterceptor { inv -> around(inv, "iteration", iterationName(inv), ::checkIterationContext) }
    }
    specsBottomToTop.forEach { s ->
      s.addSetupInterceptor { inv ->
        around(inv, "setup", "${iterationName(inv)} / ${s.name}", ::checkIterationContext)
      }
    }
    allFeatures.forEach { f ->
      f.addSetupInterceptor { inv ->
        around(inv, "feature scoped setup", iterationName(inv), ::checkIterationContext)
      }
    }
    spec.allSetupMethods.forEach { m ->
      m.addInterceptor { inv ->
        around(inv, "setup method", iterationMethodName(inv), ::checkIterationMethodContext)
      }
    }
    allFeatures.forEach { f ->
      f.featureMethod.addInterceptor { inv ->
        around(inv, "feature method", iterationMethodName(inv), ::checkIterationMethodContext)
      }
    }
    specsBottomToTop.forEach { s ->
      s.addCleanupInterceptor { inv ->
        around(inv, "cleanup", "${iterationName(inv)} / ${s.name}", ::checkIterationContext)
      }
    }
    allFeatures.forEach { f ->
      f.addCleanupInterceptor { inv ->
        around(inv, "feature scoped cleanup", iterationName(inv), ::checkIterationContext)
      }
    }
    spec.allCleanupMethods.forEach { m ->
      m.addInterceptor { inv ->
        around(inv, "cleanup method", iterationMethodName(inv), ::checkIterationMethodContext)
      }
    }
    specsBottomToTop.forEach { s ->
      s.addCleanupSpecInterceptor { inv -> around(inv, "cleanup spec", inv.spec.name, ::checkSpecContext) }
    }
    spec.allCleanupSpecMethods.forEach { m ->
      m.addInterceptor { inv ->
        around(inv, "cleanup spec method", "${inv.spec.name}.${inv.method.name}()", ::checkSpecMethodContext)
      }
    }
    spec.allFixtureMethods.forEach { m ->
      m.addInterceptor { inv ->
        val check: (IMethodInvocation) -> Unit =
          if (inv.method.name.endsWith("Spec")) ::checkSpecMethodContext else ::checkIterationMethodContext
        val prefix = inv.feature?.let { "${iterationName(inv)} / " } ?: ""
        around(inv, "fixture method", "$prefix${inv.spec.name}.${inv.method.name}()", check)
      }
    }
  }

  private fun scopedMethods(feature: FeatureInfo, methods: List<MethodInfo>, type: String) {
    methods.forEach { m ->
      feature.addScopedMethodInterceptor(m) { inv ->
        around(
          inv,
          type,
          "${iterationMethodName(inv)} / from ${feature.name}",
          ::checkIterationMethodContext
        )
      }
    }
  }

  private fun iterationName(inv: IMethodInvocation) =
    "${inv.spec.name}.${inv.feature.name}[#${inv.iteration.iterationIndex}]"

  private fun iterationMethodName(inv: IMethodInvocation) =
    "${inv.feature.parent.name}.${inv.feature.name}[#${inv.iteration.iterationIndex}] / " +
      "${inv.spec.name}.${inv.method.name}()"

  private fun around(inv: IMethodInvocation, type: String, name: String, check: (IMethodInvocation) -> Unit) {
    check(inv)
    lifecycleOutline += "${indent}$type start ($name)\n"
    indent += "  "
    inv.proceed()
    indent = indent.dropLast(2)
    lifecycleOutline += "${indent}$type end ($name)\n"
    check(inv)
  }

  private fun checkSpecCommonContext(inv: IMethodInvocation) {
    check(inv.spec != null)
    check(inv.feature == null)
    check(inv.iteration == null)
    check(inv.instance === inv.sharedInstance)
    check(inv.method != null)
    val ctx = (inv.instance as spock.lang.Specification).specificationContext
    check(ctx.currentSpec != null)
    val featureError = runCatching { ctx.currentFeature }.exceptionOrNull()
    check(featureError is IllegalStateException)
    check(featureError.message == "Cannot request current feature in @Shared context, or feature context")
    val iterationError = runCatching { ctx.currentIteration }.exceptionOrNull()
    check(iterationError is IllegalStateException)
    check(iterationError.message == "Cannot request current iteration in @Shared context, or feature context")
  }

  private fun checkSpecContext(inv: IMethodInvocation) {
    checkSpecCommonContext(inv)
    check(inv.target !== inv.instance)
    check(inv.method.reflection == null)
    check(inv.method.name.isNullOrEmpty())
  }

  private fun checkSpecMethodContext(inv: IMethodInvocation) {
    checkSpecCommonContext(inv)
    check(inv.target === inv.instance)
    check(inv.method.reflection != null)
    check(!inv.method.name.isNullOrEmpty())
  }

  private fun checkFeatureCommonContext(inv: IMethodInvocation) {
    check(inv.spec != null)
    check(inv.feature != null)
    check(inv.method != null)
    check((inv.instance as spock.lang.Specification).specificationContext.currentSpec != null)
  }

  private fun checkFeatureContext(inv: IMethodInvocation) {
    checkFeatureCommonContext(inv)
    check(inv.iteration == null)
    check(inv.instance === inv.sharedInstance)
    check(inv.target !== inv.instance)
    check(inv.method.reflection == null)
    check(inv.method.name.isNullOrEmpty())
    val ctx = (inv.instance as spock.lang.Specification).specificationContext
    val featureError = runCatching { ctx.currentFeature }.exceptionOrNull()
    check(featureError is IllegalStateException)
    check(featureError.message == "Cannot request current feature in @Shared context, or feature context")
    val iterationError = runCatching { ctx.currentIteration }.exceptionOrNull()
    check(iterationError is IllegalStateException)
    check(iterationError.message == "Cannot request current iteration in @Shared context, or feature context")
  }

  private fun checkIterationCommonContext(inv: IMethodInvocation) {
    checkFeatureCommonContext(inv)
    check(inv.iteration != null)
    check(inv.instance !== inv.sharedInstance)
    val ctx = (inv.instance as spock.lang.Specification).specificationContext
    check(ctx.currentFeature != null)
    check(ctx.currentIteration != null)
  }

  private fun checkIterationContext(inv: IMethodInvocation) {
    checkIterationCommonContext(inv)
    check(inv.target !== inv.instance)
    check(inv.method.reflection == null)
    check(inv.method.name.isNullOrEmpty())
  }

  private fun checkIterationMethodContext(inv: IMethodInvocation) {
    checkIterationCommonContext(inv)
    check(inv.target === inv.instance)
    check(inv.method.reflection != null)
    check(!inv.method.name.isNullOrEmpty())
  }
}
