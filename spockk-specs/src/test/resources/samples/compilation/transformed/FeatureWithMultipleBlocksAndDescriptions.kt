@io.github.pshevche.spockk.lang.internal.SpecMetadata
@org.spockframework.runtime.model.SpecMetadata(filename = "FeatureWithMultipleBlocksAndDescriptions.kt", line = 1)
class FeatureWithMultipleBlocksAndDescriptions {
    @io.github.pshevche.spockk.lang.internal.FeatureMetadata(0)
    @org.spockframework.runtime.model.FeatureMetadata(
        ordinal = 0,
        name = "some feature",
        line = 2,
        parameterNames = [],
        blocks = [
            org.spockframework.runtime.model.BlockMetadata(
                org.spockframework.runtime.model.BlockKind.WHEN,
                [""]
            ),
            org.spockframework.runtime.model.BlockMetadata(
                org.spockframework.runtime.model.BlockKind.THEN,
                [""]
            ),
            org.spockframework.runtime.model.BlockMetadata(
                org.spockframework.runtime.model.BlockKind.WHEN,
                ["incrementing again"]
            ),
            org.spockframework.runtime.model.BlockMetadata(
                org.spockframework.runtime.model.BlockKind.THEN,
                ["value is 3"]
            )
        ]
    )
    fun `some feature`() {
        var a = 1

        a += 1

        assert(a == 2)

        a += 1

        assert(a == 3)
    }
}
