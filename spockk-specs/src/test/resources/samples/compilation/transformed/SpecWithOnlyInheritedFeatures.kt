abstract class BaseSpec {
    @io.github.pshevche.spockk.lang.internal.FeatureMetadata(0)
    @org.spockframework.runtime.model.FeatureMetadata(
        ordinal = 0,
        name = "inherited feature",
        line = 2,
        parameterNames = [],
        blocks = [org.spockframework.runtime.model.BlockMetadata(
            org.spockframework.runtime.model.BlockKind.EXPECT,
            [""]
        )]
    )
    fun `inherited feature`() {
        assert(true)
    }
}

@io.github.pshevche.spockk.lang.internal.SpecMetadata
@org.spockframework.runtime.model.SpecMetadata(filename = "SpecWithOnlyInheritedFeatures.kt", line = 7)
class Spec : BaseSpec()
