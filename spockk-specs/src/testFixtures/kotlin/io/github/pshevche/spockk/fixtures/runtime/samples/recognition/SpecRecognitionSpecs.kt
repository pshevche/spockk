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

package io.github.pshevche.spockk.fixtures.runtime.samples.recognition

import io.github.pshevche.spockk.lang.expect
import spock.lang.Specification

class DirectSpecRecognitionSpec : Specification() {
  fun foo() {
    expect
    assert(false)
  }
}

class FullyQualifiedSpecRecognitionSpec : spock.lang.Specification() {
  fun foo() {
    expect
    assert(false)
  }
}

class IntermediarySpecRecognitionSpec : SpecRecognitionIntermediary() {
  fun foo() {
    expect
    assert(false)
  }
}

class AbstractIntermediarySpecRecognitionSpec : AbstractSpecRecognitionIntermediary() {
  fun foo() {
    expect
    assert(false)
  }
}

open class SameUnitIntermediarySpec : Specification()

class SameUnitSpecRecognitionSpec : SameUnitIntermediarySpec() {
  fun foo() {
    expect
    assert(false)
  }
}

class FullyQualifiedIntermediarySpecRecognitionSpec : io.github.pshevche.spockk.fixtures.runtime.samples.recognition.FullyQualifiedSpecRecognitionIntermediary() {
  fun foo() {
    expect
    assert(false)
  }
}
