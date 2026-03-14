@org.spockframework.runtime.model.SpecMetadata(filename = "CleanupMethod.kt", line = 1)
class CleanupMethod : spock.lang.Specification() {
  private fun cleanup() {
    println("cleanup")
  }

  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "some feature",
    line = 6,
    parameterNames = [],
    blocks = [org.spockframework.runtime.model.BlockMetadata(
      org.spockframework.runtime.model.BlockKind.EXPECT,
      [""]
    )]
  )
  fun `$spock_feature_0_0`() {
    assert(true)
  }
}
