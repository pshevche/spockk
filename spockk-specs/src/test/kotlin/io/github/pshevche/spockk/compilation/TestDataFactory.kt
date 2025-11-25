package io.github.pshevche.spockk.compilation

import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toUpperCaseAsciiOnly

object TestDataFactory {

    fun specWithFeatureBody(featureBody: String): SourceFile = kotlin(
        "Spec.kt", """
            class Spec {
                fun `some feature`() {
                    ${featureBody}
                }
            } 
        """.trimIndent()
    )

    fun specWithSingleFeature(label: String) = TransformationSample(
        kotlin(
            "Spec.kt", """
                class Spec : spock.lang.Specification() {
                    fun `some feature`() {
                        io.github.pshevche.spockk.lang.${label}
                        assert(true)
                    }
                }   
            """
        ),
        kotlin(
            "Spec.kt", """
                @org.spockframework.runtime.model.SpecMetadata(filename = "Spec.kt", line = 1)
                class Spec : spock.lang.Specification() {
                    @org.spockframework.runtime.model.FeatureMetadata(
                        ordinal = 0,
                        name = "some feature",
                        line = 2,
                        parameterNames = [],
                        blocks = [org.spockframework.runtime.model.BlockMetadata(
                            org.spockframework.runtime.model.BlockKind.${label.toUpperCaseAsciiOnly()},
                            [""]
                        )]
                    )
                    fun `some feature`() {
                        assert(true)
                    }
                }
            """
        )
    )

}
