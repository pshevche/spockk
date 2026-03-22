@org.spockframework.runtime.model.SpecMetadata(filename = "FeatureWithMultipleBlocksAndDescriptions.kt", line = 1)
class FeatureWithMultipleBlocksAndDescriptions : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "some feature",
    line = 2,
    parameterNames = [],
    blocks = [
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.SETUP,
        [""]
      ),
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
  fun `$spock_feature_0_0`() {
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    var a = 1
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 1)
    a += 1
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 1)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 2)
    assert(a == 2)
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 2)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 3)
    a += 1
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 3)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 4)
    assert(a == 3)
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 4)
  }
}
