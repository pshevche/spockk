@org.spockframework.runtime.model.SpecMetadata(filename = "SharedValField.kt", line = 1)
class SharedValField : spock.lang.Specification() {
  @org.spockframework.runtime.model.FieldMetadata(
    name = "sharedAnswer",
    ordinal = 0,
    line = 3,
    initializer = true
  )
  @Volatile
  protected var `$spock_sharedField_sharedAnswer`: Int? = null

  fun getSharedAnswer(): Int? =
    specificationContext.sharedInstance.`$spock_sharedField_sharedAnswer`

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
    assert(getSharedAnswer() == 42)
  }

  private fun `$spock_initializeSharedFields`() {
    `$spock_sharedField_sharedAnswer` = 42
  }
}
