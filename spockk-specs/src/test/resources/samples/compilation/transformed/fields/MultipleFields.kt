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
    val `$spock_valueRecorder` : org.spockframework.runtime.ValueRecorder = org.spockframework.runtime.ValueRecorder()
    val `$spock_errorCollector` : org.spockframework.runtime.ErrorCollector =
      org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    try {
      org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
        `$spock_valueRecorder`.reset(),
        "(answer == 42)",
        9,
        5,
        null,
        `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2),
          (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(0), getAnswer()) as Int) == (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), 42) as Int)) as Boolean)
    }
    catch (`$spock_condition_throwable` : Throwable) {
      org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
        `$spock_valueRecorder`,
        "(answer == 42)",
        9,
        5,
        null,
        `$spock_condition_throwable`)}
    finally {
    }
    try {
      org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
        `$spock_valueRecorder`.reset(),
        "(instanceField == \"hello\")",
        10,
        5,
        null,
        `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2),
          (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(0), instanceField) as String) == (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), "hello") as String)) as Boolean)
    }
    catch (`$spock_condition_throwable` : Throwable) {
      org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
        `$spock_valueRecorder`,
        "(instanceField == \"hello\")",
        10,
        5,
        null,
        `$spock_condition_throwable`)}
    finally {
    }
    try {
      org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
        `$spock_valueRecorder`.reset(),
        "(sharedField == 24)",
        11,
        5,
        null,
        `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2),
          (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(0), getSharedField()) as Int) == (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), 24) as Int)) as Boolean)
    }
    catch (`$spock_condition_throwable` : Throwable) {
      org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
        `$spock_valueRecorder`,
        "(sharedField == 24)",
        11,
        5,
        null,
        `$spock_condition_throwable`)}
    finally {
    }
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
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
