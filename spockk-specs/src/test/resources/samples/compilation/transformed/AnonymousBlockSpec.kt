@org.spockframework.runtime.model.SpecMetadata(filename = "AnonymousBlockSpec.kt", line = 1)
class AnonymousBlockSpec : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "feature with implicit given",
    line = 2,
    parameterNames = [],
    blocks = [org.spockframework.runtime.model.BlockMetadata(
      org.spockframework.runtime.model.BlockKind.EXPECT,
      [""]
    )]
  )
  fun `$spock_feature_0_0`() {
    val a = 1
    val b = 2
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    assert(a + b == 3)
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
  }
}
