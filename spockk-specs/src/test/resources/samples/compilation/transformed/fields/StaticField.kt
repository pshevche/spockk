@org.spockframework.runtime.model.SpecMetadata(filename = "StaticField.kt", line = 1)
class StaticField : spock.lang.Specification() {
  companion object {
    val staticField = "static"
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
    assert(staticField == "static")
  }
}
