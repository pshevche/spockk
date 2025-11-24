abstract class BaseSpec {
    @io.github.pshevche.spockk.lang.internal.FeatureMetadata(0)
    @org.spockframework.runtime.model.FeatureMetadata(
        ordinal = 0,
        name = "inherited feature 1",
        line = 2,
        parameterNames = [],
        blocks = [org.spockframework.runtime.model.BlockMetadata(
            org.spockframework.runtime.model.BlockKind.EXPECT,
            [""]
        )]
    )
    fun `inherited feature 1`() {
        assert(true)
    }
}

abstract class IntermediateSpec : BaseSpec() {
    @io.github.pshevche.spockk.lang.internal.FeatureMetadata(1)
    @org.spockframework.runtime.model.FeatureMetadata(
        ordinal = 1,
        name = "inherited feature 2",
        line = 9,
        parameterNames = [],
        blocks = [org.spockframework.runtime.model.BlockMetadata(
            org.spockframework.runtime.model.BlockKind.EXPECT,
            [""]
        )]
    )
    fun `inherited feature 2`() {
        assert(true)
    }
}

@io.github.pshevche.spockk.lang.internal.SpecMetadata
@org.spockframework.runtime.model.SpecMetadata(filename = "SpecWithOwnAndInheritedFeatures.kt", line = 15)
class Spec : IntermediateSpec() {
    @io.github.pshevche.spockk.lang.internal.FeatureMetadata(2)
    @org.spockframework.runtime.model.FeatureMetadata(
        ordinal = 2,
        name = "own feature",
        line = 16,
        parameterNames = [],
        blocks = [org.spockframework.runtime.model.BlockMetadata(
            org.spockframework.runtime.model.BlockKind.EXPECT,
            [""]
        )]
    )
    fun `own feature`() {
        assert(true)
    }
}
