class Assertions {
  @spock.lang.VerifyAll
  fun isPositiveAndEven(a: Int) {
    val `$spock_valueRecorder` : org.spockframework.runtime.ValueRecorder = org.spockframework.runtime.ValueRecorder()
    val `$spock_errorCollector` : org.spockframework.runtime.ErrorCollector = org.spockframework.runtime.ErrorCollector()
    try {
      try {
        org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
          `$spock_valueRecorder`.reset(),
          "a > 0",
          4,
          5,
          null,
          `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2),
            (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(0), a) as Int) > (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), 0) as Int)) as Boolean)
      }
      catch (`$spock_condition_throwable` : Throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
          `$spock_valueRecorder`,
          "a > 0",
          4,
          5,
          null,
          `$spock_condition_throwable`)
      }
      finally {
      }
      try {
        org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
          `$spock_valueRecorder`.reset(),
          "a % 2 == 0",
          5,
          5,
          null,
          `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(4),
            (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2),
              (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(0), a) as Int) % (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), 2) as Int)) as Int) == (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(3), 0) as Int)) as Boolean)
      }
      catch (`$spock_condition_throwable` : Throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
          `$spock_valueRecorder`,
          "a % 2 == 0",
          5,
          5,
          null,
          `$spock_condition_throwable`)
      }
      finally {
      }
    }
    finally {
      `$spock_errorCollector`.validateCollectedErrors()
    }
  }
}
