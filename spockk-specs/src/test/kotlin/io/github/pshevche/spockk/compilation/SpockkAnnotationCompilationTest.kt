package io.github.pshevche.spockk.compilation

import io.github.pshevche.spockk.compilation.TestDataFactory.specWithSingleFeature
import io.github.pshevche.spockk.compilation.TransformationSample.Companion.sampleFromResource
import io.github.pshevche.spockk.lang.expect

class SpockkAnnotationCompilationTest : BaseCompilationTest() {

    fun `keeps classes without spockk labels untransformed`() {
        expect
        assertTransformation(sampleFromResource("NonSpec"))
    }

    fun `annotates classes with feature methods with @SpecMetadata`() {
        expect
        assertTransformation(sampleFromResource("SingleFeatureSpec"))
    }

    fun `annotates features with spockk labels with @FeatureMetadata`() {
        expect
        assertTransformation(specWithSingleFeature("expect"))
    }

    fun `annotates abstract and open spec classes`() {
        expect
        assertTransformation(sampleFromResource("AbstractBaseSpec"))
        assertTransformation(sampleFromResource("OpenBaseSpec"))
    }

    fun `annotates child classes with @SpecMetadata if parent contains features`() {
        expect
        assertTransformation(sampleFromResource("SpecWithOnlyInheritedFeatures"))
    }

    fun `captures blocks and their descriptions`() {
        expect
        assertTransformation(sampleFromResource("FeatureWithMultipleBlocksAndDescriptions"))
    }

}
