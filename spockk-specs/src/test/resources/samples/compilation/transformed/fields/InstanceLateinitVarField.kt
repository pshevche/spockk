@org.spockframework.runtime.model.SpecMetadata(filename = "InstanceLateinitVarField.kt", line = 1)
class InstanceLateinitVarField : spock.lang.Specification() {
  @org.spockframework.runtime.model.FieldMetadata(
    name = "uninitializedField",
    ordinal = 0,
    line = 2,
    initializer = false
  )
  private lateinit var uninitializedField: String

  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "some feature",
    line = 4,
    parameterNames = [],
    blocks = [org.spockframework.runtime.model.BlockMetadata(
      org.spockframework.runtime.model.BlockKind.EXPECT,
      [""]
    )]
  )
  fun `$spock_feature_0_0`() {
    uninitializedField = "initialized"
    assert(uninitializedField == "initialized")
  }
}
