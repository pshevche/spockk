abstract class AbstractBaseSpec {
    @io.github.pshevche.spockk.lang.internal.FeatureMetadata(0)
    @org.spockframework.runtime.model.FeatureMetadata(
        ordinal = 0,
        name = "some feature",
        line = 2,
        parameterNames = [],
        blocks = [org.spockframework.runtime.model.BlockMetadata(
            org.spockframework.runtime.model.BlockKind.EXPECT,
            [""]
        )]
    )
    fun `some feature`() {
        assert(true)
    }
}
