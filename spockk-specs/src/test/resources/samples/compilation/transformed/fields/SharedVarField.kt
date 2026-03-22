@org.spockframework.runtime.model.SpecMetadata(filename = "SharedVarField.kt", line = 1)
class SharedVarField : spock.lang.Specification() {
  @spock.lang.Shared
  @org.spockframework.runtime.model.FieldMetadata(
    name = "sharedField",
    ordinal = 0,
    line = 3,
    initializer = true
  )
  @kotlin.jvm.Volatile
  protected var `$spock_sharedField_sharedField`: Int? = null

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
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    assert(getSharedField() == 42)
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
  }

  private fun `$spock_initializeSharedFields`() {
    `$spock_sharedField_sharedField` = 42
  }

  fun getSharedField(): Int? =
    ((specificationContext as org.spockframework.runtime.SpecificationContext).sharedInstance as SharedVarField).`$spock_sharedField_sharedField`

  fun setSharedField(value: Int?) {
    ((specificationContext as org.spockframework.runtime.SpecificationContext).sharedInstance as SharedVarField).`$spock_sharedField_sharedField` = value
  }
}
