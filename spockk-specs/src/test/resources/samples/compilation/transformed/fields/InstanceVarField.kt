@org.spockframework.runtime.model.SpecMetadata(filename = "InstanceVarField.kt", line = 1)
class InstanceVarField : spock.lang.Specification() {
  @org.spockframework.runtime.model.FieldMetadata(
    name = "instanceField",
    ordinal = 0,
    line = 2,
    initializer = true
  )
  private var instanceField: String? = null

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
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    assert(instanceField == "hello")
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
  }

  private fun `$spock_initializeFields`() {
    instanceField = "hello"
  }
}
