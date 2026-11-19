class Assertions {
  companion object {
    @JvmStatic
    @spock.lang.Verify
    fun isPositive(a: Int) {
      val `$spock_valueRecorder` : org.spockframework.runtime.ValueRecorder = org.spockframework.runtime.ValueRecorder()
      val `$spock_errorCollector` : org.spockframework.runtime.ErrorCollector =
        org.spockframework.runtime.ErrorRethrower.INSTANCE
      try {
        org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
          `$spock_valueRecorder`.reset(),
          "a > 0",
          6,
          7,
          null,
          `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2), (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(0), a) as Int) > (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), 0) as Int)) as Boolean)
      }
      catch (`$spock_condition_throwable` : Throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
          `$spock_valueRecorder`,
          "a > 0",
          6,
          7,
          null,
          `$spock_condition_throwable`)
      }
      finally {
      }
    }

    @JvmStatic
    fun nonAssertion() {
      println("nonAssertion")
    }
  }
}
