@org.spockframework.runtime.model.SpecMetadata(filename = "SharedVarField.kt", line = 1)
class SharedVarField : spock.lang.Specification() {
  @org.spockframework.runtime.model.FieldMetadata(
    name = "sharedField",
    ordinal = 0,
    line = 3,
    initializer = true
  )
  @Volatile
  protected var `$spock_sharedField_sharedField`: Int? = null

  fun getSharedField(): Int? =
    specificationContext.sharedInstance.`$spock_sharedField_sharedField`

  fun setSharedField(value: Int?) {
    specificationContext.sharedInstance.`$spock_sharedField_sharedField` = value
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
    assert(getSharedField() == 42)
  }

  private fun `$spock_initializeSharedFields`() {
    `$spock_sharedField_sharedField` = 42
  }
}
