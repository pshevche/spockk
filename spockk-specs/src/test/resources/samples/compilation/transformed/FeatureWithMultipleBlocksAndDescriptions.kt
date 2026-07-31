@org.spockframework.runtime.model.SpecMetadata(filename = "FeatureWithMultipleBlocksAndDescriptions.kt", line = 1)
class FeatureWithMultipleBlocksAndDescriptions : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "some feature",
    line = 2,
    parameterNames = [],
    blocks = [
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.SETUP,
        [""]
      ),
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.WHEN,
        [""]
      ),
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.THEN,
        [""]
      ),
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.WHEN,
        ["incrementing again"]
      ),
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.THEN,
        ["value is 3"]
      )
    ]
  )
  fun `$spock_feature_0_0`() {
    val `$spock_valueRecorder` : org.spockframework.runtime.ValueRecorder = org.spockframework.runtime.ValueRecorder()
    val `$spock_errorCollector` : org.spockframework.runtime.ErrorCollector =
      org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    var a = 1
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 1)
    a += 1
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 1)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 2)
    try {
      org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
        `$spock_valueRecorder`.reset(),
        "(a == 2)",
        10,
        5,
        null,
        `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2),
          (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(0), a) as Int) == (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), 2) as Int)) as Boolean)
    }
    catch (`$spock_condition_throwable` : Throwable) {
      org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
        `$spock_valueRecorder`,
        "(a == 2)",
        10,
        5,
        null,
        `$spock_condition_throwable`)}
    finally {
    }
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 2)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 3)
    a += 1
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 3)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 4)
    try {
      org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
        `$spock_valueRecorder`.reset(),
        "(a == 3)",
        16,
        5,
        null,
        `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2),
          (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(0), a) as Int) == (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), 3) as Int)) as Boolean)
    }
    catch (`$spock_condition_throwable` : Throwable) {
      org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
        `$spock_valueRecorder`,
        "(a == 3)",
        16,
        5,
        null,
        `$spock_condition_throwable`)}
    finally {
    }
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 4)
  }
}
