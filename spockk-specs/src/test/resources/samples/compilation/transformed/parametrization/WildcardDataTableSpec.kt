@org.spockframework.runtime.model.SpecMetadata(filename = "WildcardDataTableSpec.kt", line = 1)
class WildcardDataTableSpec : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "can use pseudo-column to enable one-column table",
    line = 2,
    parameterNames = ["a"],
    blocks = [org.spockframework.runtime.model.BlockMetadata(
      org.spockframework.runtime.model.BlockKind.EXPECT,
      [""]
    ),
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.WHERE,
        [""]
      )]
  )
  fun `$spock_feature_0_0`(a: Int) {
    val `$spock_valueRecorder` : org.spockframework.runtime.ValueRecorder = org.spockframework.runtime.ValueRecorder()
    val `$spock_errorCollector` : org.spockframework.runtime.ErrorCollector =
      org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    try {
      org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
        `$spock_valueRecorder`.reset(),
        "a == 1",
        4,
        5,
        null,
        `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2),
          (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(0), a) as Int) == (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), 1) as Int)) as Boolean)
    }
    catch (`$spock_condition_throwable` : Throwable) {
      org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
        `$spock_valueRecorder`,
        "a == 1",
        4,
        5,
        null,
        `$spock_condition_throwable`)}
    finally {
    }
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
  }

  @org.spockframework.runtime.model.DataProviderMetadata(line = 0,
    dataVariables = ["a"],
    previousDataTableVariables = [])
  fun `$spock_feature_0_0prov0`(): Any {
    return listOf<Int>(1)
  }

  @org.spockframework.runtime.model.DataProcessorMetadata(dataVariables = ["a"])
  fun `$spock_feature_0_0proc`(spock_p0: Any): Any {
    var a: Int
    a = (( spock_p0 ) as Int)
    return arrayOf<Any>(a)
  }

  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 1,
    name = "pseudo-column can be declared as parameter",
    line = 11,
    parameterNames = ["a", "_"],
    blocks = [org.spockframework.runtime.model.BlockMetadata(
      org.spockframework.runtime.model.BlockKind.EXPECT,
      [""]
    ),
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.WHERE,
        [""]
      )]
  )
  fun `$spock_feature_0_1`(a: Int, `_`: Any) {
    val `$spock_valueRecorder` : org.spockframework.runtime.ValueRecorder = org.spockframework.runtime.ValueRecorder()
    val `$spock_errorCollector` : org.spockframework.runtime.ErrorCollector =
      org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    try {
      org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
        `$spock_valueRecorder`.reset(),
        "a == 3",
        13,
        5,
        null,
        `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2),
          (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(0), a) as Int) == (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), 3) as Int)) as Boolean)
    }
    catch (`$spock_condition_throwable` : Throwable) {
      org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
        `$spock_valueRecorder`,
        "a == 3",
        13,
        5,
        null,
        `$spock_condition_throwable`)}
    finally {
    }
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
  }

  @org.spockframework.runtime.model.DataProviderMetadata(line = 0,
    dataVariables = ["a"],
    previousDataTableVariables = [])
  fun `$spock_feature_0_1prov0`(): Any {
    return listOf<Int>(3)
  }

  @org.spockframework.runtime.model.DataProviderMetadata(line = 0,
    dataVariables = ["_"],
    previousDataTableVariables = ["a"])
  fun `$spock_feature_0_1prov1`(spock_p_a: List<Int>): Any {
    return listOf<Any>(`_`)
  }

  @org.spockframework.runtime.model.DataProcessorMetadata(dataVariables = ["a", "_"])
  fun `$spock_feature_0_1proc`(spock_p0: Any, spock_p1: Any): Any {
    var a: Int
    a = (( spock_p0 ) as Int)
    var `_`: Any
    `_` = (( spock_p1 ) as Any)
    return arrayOf<Any>(a, `_`)
  }
}

