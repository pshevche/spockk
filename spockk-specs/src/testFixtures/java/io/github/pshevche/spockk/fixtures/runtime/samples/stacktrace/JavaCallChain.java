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

package io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace;

// Must stay line-for-line identical to KotlinCallChain.kt.
public class JavaCallChain {
  public void a() {
    b();
  }

  public void b() {
    new Inner().inner();
  }

  public void c() { throw new CallChainException(); }

  public class Inner {
    public void inner() {
      new StaticInner().staticInner();
    }
  }

  public static class StaticInner {
    public void staticInner() {
      new JavaCallChain().c();
    }
  }
}
