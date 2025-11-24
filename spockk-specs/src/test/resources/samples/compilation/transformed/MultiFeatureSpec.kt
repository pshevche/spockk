@io.github.pshevche.spockk.lang.internal.SpecMetadata
@org.spockframework.runtime.model.SpecMetadata(filename = "MultiFeatureSpec.kt", line = 1)
class MultiFeatureSpec {
    @io.github.pshevche.spockk.lang.internal.FeatureMetadata(0)
    @org.spockframework.runtime.model.FeatureMetadata(
        ordinal = 0,
        name = "feature 1",
        line = 2,
        parameterNames = [],
        blocks = [org.spockframework.runtime.model.BlockMetadata(
            org.spockframework.runtime.model.BlockKind.EXPECT,
            [""]
        )]
    )
    fun `feature 1`() {
        assert(true)
    }

    @io.github.pshevche.spockk.lang.internal.FeatureMetadata(1)
    @org.spockframework.runtime.model.FeatureMetadata(
        ordinal = 1,
        name = "feature 2",
        line = 7,
        parameterNames = [],
        blocks = [org.spockframework.runtime.model.BlockMetadata(
            org.spockframework.runtime.model.BlockKind.EXPECT,
            [""]
        )]
    )
    fun `feature 2`() {
        assert(true)
    }

    @io.github.pshevche.spockk.lang.internal.FeatureMetadata(2)
    @org.spockframework.runtime.model.FeatureMetadata(
        ordinal = 2,
        name = "feature 3",
        line = 12,
        parameterNames = [],
        blocks = [org.spockframework.runtime.model.BlockMetadata(
            org.spockframework.runtime.model.BlockKind.EXPECT,
            [""]
        )]
    )
    fun `feature 3`() {
        assert(true)
    }
}
