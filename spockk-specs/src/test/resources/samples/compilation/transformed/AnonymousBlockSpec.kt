@org.spockframework.runtime.model.SpecMetadata(filename = "AnonymousBlockSpec.kt", line = 1)
class AnonymousBlockSpec : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "feature with implicit given",
    line = 2,
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
    val a = 1
    val b = 2
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    try {
      org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
        `$spock_valueRecorder`.reset(),
        "(a + b == 3)",
        7,
        5,
        null,
        `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(4),
          (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2),
            (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(0), a) as Int) + (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), b) as Int)) as Int) == (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(3), 3) as Int)) as Boolean)
    }
    catch (`$spock_condition_throwable` : Throwable) {
      org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
        `$spock_valueRecorder`,
        "(a + b == 3)",
        7,
        5,
        null,
        `$spock_condition_throwable`)}
    finally {
    }
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
  }
}
