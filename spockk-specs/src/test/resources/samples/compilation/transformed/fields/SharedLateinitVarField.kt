@org.spockframework.runtime.model.SpecMetadata(filename = "SharedLateinitVarField.kt", line = 1)
class SharedLateinitVarField : spock.lang.Specification() {
  @org.spockframework.runtime.model.FieldMetadata(
    name = "uninitializedSharedField",
    ordinal = 0,
    line = 3,
    initializer = false
  )
  @Volatile
  protected lateinit var `$spock_sharedField_uninitializedSharedField`: String

  fun getUninitializedSharedField(): String =
    specificationContext.sharedInstance.`$spock_sharedField_uninitializedSharedField`

  fun setUninitializedSharedField(value: String) {
    specificationContext.sharedInstance.`$spock_sharedField_uninitializedSharedField` = value
  }

  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "some feature",
    line = 5,
    parameterNames = [],
    blocks = [org.spockframework.runtime.model.BlockMetadata(
      org.spockframework.runtime.model.BlockKind.EXPECT,
      [""]
    )]
  )
  fun `$spock_feature_0_0`() {
    setUninitializedSharedField("initialized")
    assert(getUninitializedSharedField() == "initialized")
  }
}
