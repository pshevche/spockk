@org.spockframework.runtime.model.SpecMetadata(filename = "InstanceLateinitVarField.kt", line = 1)
class InstanceLateinitVarField : spock.lang.Specification() {
  @org.spockframework.runtime.model.FieldMetadata(
    name = "uninitializedField",
    ordinal = 0,
    line = 2,
    initializer = false
  )
  private lateinit var uninitializedField: String

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
    val `$spock_valueRecorder` : org.spockframework.runtime.ValueRecorder = org.spockframework.runtime.ValueRecorder()
    val `$spock_errorCollector` : org.spockframework.runtime.ErrorCollector =
      org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    uninitializedField = "initialized"
    try {
      org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
        `$spock_valueRecorder`.reset(),
        "(uninitializedField == \"initialized\")",
        7,
        5,
        null,
        `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2),
          (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(0), uninitializedField) as String) == (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), "initialized") as String)) as Boolean)
    }
    catch (`$spock_condition_throwable` : Throwable) {
      org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
        `$spock_valueRecorder`,
        "(uninitializedField == \"initialized\")",
        7,
        5,
        null,
        `$spock_condition_throwable`)}
    finally {
    }
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
  }
}
