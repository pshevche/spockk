@org.spockframework.runtime.model.SpecMetadata(filename = "CleanupSpecMethod.kt", line = 1)
class CleanupSpecMethod : spock.lang.Specification() {
  private fun cleanupSpec() {
    println("cleanupSpec")
  }

  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "some feature",
    line = 6,
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
        "(true)",
        8,
        5,
        null,
        `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(0), true) as Boolean)
    }
    catch (`$spock_condition_throwable` : Throwable) {
      org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
        `$spock_valueRecorder`,
        "(true)",
        8,
        5,
        null,
        `$spock_condition_throwable`)}
    finally {
    }
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
  }
}
