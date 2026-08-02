@org.spockframework.runtime.model.SpecMetadata(filename = "SharedValField.kt", line = 1)
class SharedValField : spock.lang.Specification() {
  @spock.lang.Shared
  @org.spockframework.runtime.model.FieldMetadata(
    name = "sharedAnswer",
    ordinal = 0,
    line = 3,
    initializer = true
  )
  @kotlin.jvm.Volatile
  protected var `$spock_sharedField_sharedAnswer`: Int? = null

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
    val `$spock_valueRecorder` : org.spockframework.runtime.ValueRecorder = org.spockframework.runtime.ValueRecorder()
    val `$spock_errorCollector` : org.spockframework.runtime.ErrorCollector =
      org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    try {
      org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
        `$spock_valueRecorder`.reset(),
        "(sharedAnswer == 42)",
        7,
        5,
        null,
        `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2),
          (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(0), getSharedAnswer()) as Int) == (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), 42) as Int)) as Boolean)
    }
    catch (`$spock_condition_throwable` : Throwable) {
      org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
        `$spock_valueRecorder`,
        "(sharedAnswer == 42)",
        7,
        5,
        null,
        `$spock_condition_throwable`)}
    finally {
    }
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
  }

  private fun `$spock_initializeSharedFields`() {
    `$spock_sharedField_sharedAnswer` = 42
  }

  fun getSharedAnswer(): Int? =
    ((specificationContext as org.spockframework.runtime.SpecificationContext).sharedInstance as SharedValField).`$spock_sharedField_sharedAnswer`
}
