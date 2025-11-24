/*
 * Copyright 2025 the original author or authors.
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

package io.github.pshevche.spockk.compilation.common

internal enum class FeatureBlockLabel(val displayName: String, val fqn: String, val blockKind: String?) {
    GIVEN("given", "io.github.pshevche.spockk.lang.given", null),
    WHEN("when", "io.github.pshevche.spockk.lang.when", "WHEN"),
    THEN("then", "io.github.pshevche.spockk.lang.then", "THEN"),
    EXPECT("expect", "io.github.pshevche.spockk.lang.expect", "EXPECT"),
    AND("and", "io.github.pshevche.spockk.lang.and", null);

    companion object {
        fun from(fqn: String) = entries.find { fqn == it.fqn }
    }

}
