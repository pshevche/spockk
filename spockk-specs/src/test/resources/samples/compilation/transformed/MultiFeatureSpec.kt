@org.spockframework.runtime.model.SpecMetadata(filename = "MultiFeatureSpec.kt", line = 1)
class MultiFeatureSpec : spock.lang.Specification() {
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
