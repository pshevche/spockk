@org.spockframework.runtime.model.SpecMetadata(filename = "MultipleFields.kt", line = 1)
class MultipleFields : spock.lang.Specification() {
  @org.spockframework.runtime.model.FieldMetadata(
    name = "answer",
    ordinal = 0,
    line = 2,
    initializer = true
  )
  private var `$spock_finalField_answer`: Int? = null

  fun getAnswer(): Int? = `$spock_finalField_answer`

  @org.spockframework.runtime.model.FieldMetadata(
    name = "instanceField",
    ordinal = 1,
    line = 3,
    initializer = true
  )
  private var instanceField: String? = null

  @org.spockframework.runtime.model.FieldMetadata(
    name = "sharedField",
    ordinal = 2,
    line = 5,
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
    line = 7,
    parameterNames = [],
    blocks = [org.spockframework.runtime.model.BlockMetadata(
      org.spockframework.runtime.model.BlockKind.EXPECT,
      [""]
    )]
  )
  fun `$spock_feature_0_0`() {
    assert(getAnswer() == 42)
    assert(instanceField == "hello")
    assert(getSharedField() == 24)
  }

  private fun `$spock_initializeFields`() {
    `$spock_finalField_answer` = 42
    instanceField = "hello"
  }

  private fun `$spock_initializeSharedFields`() {
    `$spock_sharedField_sharedField` = 24
  }
}
