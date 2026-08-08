@org.spockframework.runtime.model.SpecMetadata(filename = "VerifyAllBasic.kt", line = 1)
class VerifyAllBasic : spock.lang.Specification() {
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
    val `$spock_valueRecorder` : org.spockframework.runtime.ValueRecorder = org.spockframework.runtime.ValueRecorder()
    val `$spock_errorCollector` : org.spockframework.runtime.ErrorCollector =
      org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    io.github.pshevche.spockk.lang.verifyAll(1) {
      val `$spock_verifyAllErrorCollector` : org.spockframework.runtime.ErrorCollector = org.spockframework.runtime.ErrorCollector()
      try {
        org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_verifyAllErrorCollector`,
          `$spock_valueRecorder`.reset(),
          "this == 1",
          4,
          51,
          null,
          `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1),
            this == (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(0), 1) as Int)) as Boolean)
      }
      catch (`$spock_condition_throwable` : Throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_verifyAllErrorCollector`,
          `$spock_valueRecorder`,
          "this == 1",
          4,
          51,
          null,
          `$spock_condition_throwable`)
      }
      finally {
      }
      `$spock_verifyAllErrorCollector`.validateCollectedErrors()
    }
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
  }
}
