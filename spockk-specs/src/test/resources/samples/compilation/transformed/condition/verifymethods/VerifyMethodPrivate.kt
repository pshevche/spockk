@org.spockframework.runtime.model.SpecMetadata(filename = "VerifyMethodPrivate.kt", line = 1)
class VerifyMethodPrivate : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "foo",
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
    try {
      org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
        `$spock_valueRecorder`.reset(),
        "listOf(true, false).any { it }",
        4,
        5,
        null,
        `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(8), (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(4), listOf(`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), true) as Boolean, `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2), false) as Boolean)) as List<Boolean>).any { it }) as Boolean)
    }
    catch (`$spock_condition_throwable` : Throwable) {
      org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
        `$spock_valueRecorder`,
        "listOf(true, false).any { it }",
        4,
        5,
        null,
        `$spock_condition_throwable`)
    }
    finally {
    }
    if (true) {
      listOf(true, false).any { it }
    }
    io.github.pshevche.spockk.lang.verify(this) {
      try {
        org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
          `$spock_valueRecorder`.reset(),
          "listOf(true, false).any { it }",
          9,
          7,
          null,
          `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(8), (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(4), listOf(`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), true) as Boolean, `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2), false) as Boolean)) as List<Boolean>).any { it }) as Boolean)
      }
      catch (`$spock_condition_throwable` : Throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
          `$spock_valueRecorder`,
          "listOf(true, false).any { it }",
          9,
          7,
          null,
          `$spock_condition_throwable`)
      }
      finally {
      }
      if (true) {
        try {
          org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
            `$spock_valueRecorder`.reset(),
            "listOf(true, false).any { it }",
            11,
            9,
            null,
            `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(8), (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(4), listOf(`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), true) as Boolean, `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2), false) as Boolean)) as List<Boolean>).any { it }) as Boolean)
        }
        catch (`$spock_condition_throwable` : Throwable) {
          org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
            `$spock_valueRecorder`,
            "listOf(true, false).any { it }",
            11,
            9,
            null,
            `$spock_condition_throwable`)
        }
        finally {
        }
      }
    }
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
  }

  @spock.lang.Verify
  private fun isPositive(a: Int) {
    val `$spock_valueRecorder` : org.spockframework.runtime.ValueRecorder = org.spockframework.runtime.ValueRecorder()
    val `$spock_errorCollector` : org.spockframework.runtime.ErrorCollector =
      org.spockframework.runtime.ErrorRethrower.INSTANCE
    try {
      org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
        `$spock_valueRecorder`.reset(),
        "a > 0",
        18,
        5,
        null,
        `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2), (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(0), a) as Int) > (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), 0) as Int)) as Boolean)
    }
    catch (`$spock_condition_throwable` : Throwable) {
      org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
        `$spock_valueRecorder`,
        "a > 0",
        18,
        5,
        null,
        `$spock_condition_throwable`)
    }
    finally {
    }
    try {
      org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
        `$spock_valueRecorder`.reset(),
        "listOf(true, false).any { it }",
        19,
        5,
        null,
        `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(8), (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(4), listOf(`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), true) as Boolean, `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2), false) as Boolean)) as List<Boolean>).any { it }) as Boolean)
    }
    catch (`$spock_condition_throwable` : Throwable) {
      org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
        `$spock_valueRecorder`,
        "listOf(true, false).any { it }",
        19,
        5,
        null,
        `$spock_condition_throwable`)
    }
    finally {
    }
    if (true) {
      try {
        org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
          `$spock_valueRecorder`.reset(),
          "listOf(true, false).any { it }",
          21,
          7,
          null,
          `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(8), (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(4), listOf(`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), true) as Boolean, `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2), false) as Boolean)) as List<Boolean>).any { it }) as Boolean)
      }
      catch (`$spock_condition_throwable` : Throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
          `$spock_valueRecorder`,
          "listOf(true, false).any { it }",
          21,
          7,
          null,
          `$spock_condition_throwable`)
      }
      finally {
      }
    }
    io.github.pshevche.spockk.lang.verify(this) {
      try {
        org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
          `$spock_valueRecorder`.reset(),
          "listOf(true, false).any { it }",
          24,
          7,
          null,
          `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(8), (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(4), listOf(`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), true) as Boolean, `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2), false) as Boolean)) as List<Boolean>).any { it }) as Boolean)
      }
      catch (`$spock_condition_throwable` : Throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
          `$spock_valueRecorder`,
          "listOf(true, false).any { it }",
          24,
          7,
          null,
          `$spock_condition_throwable`)
      }
      finally {
      }
      if (true) {
        try {
          org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
            `$spock_valueRecorder`.reset(),
            "listOf(true, false).any { it }",
            26,
            9,
            null,
            `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(8), (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(4), listOf(`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), true) as Boolean, `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2), false) as Boolean)) as List<Boolean>).any { it }) as Boolean)
        }
        catch (`$spock_condition_throwable` : Throwable) {
          org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
            `$spock_valueRecorder`,
            "listOf(true, false).any { it }",
            26,
            9,
            null,
            `$spock_condition_throwable`)
        }
        finally {
        }
      }
    }
  }
}
