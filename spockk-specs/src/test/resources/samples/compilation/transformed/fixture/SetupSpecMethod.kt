@org.spockframework.runtime.model.SpecMetadata(filename = "SetupSpecMethod.kt", line = 1)
class SetupSpecMethod : spock.lang.Specification() {
  private fun setupSpec() {
    println("setupSpec")
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
