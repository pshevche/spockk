@org.spockframework.runtime.model.SpecMetadata(filename = "InstanceValField.kt", line = 1)
class InstanceValField : spock.lang.Specification() {
  @org.spockframework.runtime.model.FieldMetadata(
    name = "answer",
    ordinal = 0,
    line = 2,
    initializer = true
  )
  private var `$spock_finalField_answer`: Int? = null

  fun getAnswer(): Int? = `$spock_finalField_answer`

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
    assert(getAnswer() == 42)
  }

  private fun `$spock_initializeFields`() {
    `$spock_finalField_answer` = 42
  }
}
