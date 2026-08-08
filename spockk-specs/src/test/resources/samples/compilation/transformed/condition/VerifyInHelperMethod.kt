@org.spockframework.runtime.model.SpecMetadata(filename = "VerifyInHelperMethod.kt", line = 1)
class VerifyInHelperMethod : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "some feature",
    line = 2,
    parameterNames = [],
    blocks = [org.spockframework.runtime.model.BlockMetadata(
      org.spockframework.runtime.model.BlockKind.EXPECT,
      [""]
    )]
  )
  fun `$spock_feature_0_0`() {
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    checkValue()
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
  }

  private fun checkValue() {
    val `$spock_valueRecorder` : org.spockframework.runtime.ValueRecorder = org.spockframework.runtime.ValueRecorder()
    val `$spock_errorCollector` : org.spockframework.runtime.ErrorCollector =
      org.spockframework.runtime.ErrorRethrower.INSTANCE
    io.github.pshevche.spockk.lang.verify(1) {
      try {
        org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
          `$spock_valueRecorder`.reset(),
          "this == 1",
          8,
          48,
          null,
          `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1),
            this == (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(0), 1) as Int)) as Boolean)
      }
      catch (`$spock_condition_throwable` : Throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
          `$spock_valueRecorder`,
          "this == 1",
          8,
          48,
          null,
          `$spock_condition_throwable`)
      }
      finally {
      }
    }
  }
}
