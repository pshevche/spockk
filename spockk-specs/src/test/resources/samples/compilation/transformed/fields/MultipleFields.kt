@org.spockframework.runtime.model.SpecMetadata(filename = "MultipleFields.kt", line = 1)
class MultipleFields : spock.lang.Specification() {
  @org.spockframework.runtime.model.FieldMetadata(
    name = "answer",
    ordinal = 0,
    line = 2,
    initializer = true
  )
  private var `$spock_finalField_answer`: Int? = null

  @org.spockframework.runtime.model.FieldMetadata(
    name = "instanceField",
    ordinal = 1,
    line = 3,
    initializer = true
  )
  private var instanceField: String? = null

  @spock.lang.Shared
  @org.spockframework.runtime.model.FieldMetadata(
    name = "sharedField",
    ordinal = 2,
    line = 5,
    initializer = true
  )
  @kotlin.jvm.Volatile
  protected var `$spock_sharedField_sharedField`: Int? = null

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
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    assert(getAnswer() == 42)
    assert(instanceField == "hello")
    assert(getSharedField() == 24)
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
    (this.getSpecificationContext().getMockController() as org.spockframework.mock.runtime.MockController).leaveScope()
  }

  private fun `$spock_initializeFields`() {
    `$spock_finalField_answer` = 42
    instanceField = "hello"
  }

  fun getAnswer(): Int? = `$spock_finalField_answer`

  private fun `$spock_initializeSharedFields`() {
    `$spock_sharedField_sharedField` = 24
  }

  fun getSharedField(): Int? =
    ((specificationContext as org.spockframework.runtime.SpecificationContext).sharedInstance as MultipleFields).`$spock_sharedField_sharedField`
}
