import io.github.pshevche.spockk.lang.*

@org.spockframework.runtime.model.SpecMetadata(filename = "MultiPipeMultiVariableSpec.kt", line = 3)
class MultiPipeMultiVariableSpec : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "multiple data pipes",
    line = 4,
    parameterNames = ["a", "b", "c"],
    blocks = [org.spockframework.runtime.model.BlockMetadata(
      org.spockframework.runtime.model.BlockKind.EXPECT,
      [""]
    ),
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.WHERE,
        [""]
      )]
  )
  fun `$spock_feature_0_0`(a: Int, b: Int, c: Int) {
    val `$spock_valueRecorder` : org.spockframework.runtime.ValueRecorder = org.spockframework.runtime.ValueRecorder()
    val `$spock_errorCollector` : org.spockframework.runtime.ErrorCollector =
      org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    try {
      org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
        `$spock_valueRecorder`.reset(),
        "a + b == c",
        6,
        5,
        null,
        `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(4),
          (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2),
            (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(0), a) as Int) + (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), b) as Int)) as Int) == (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(3), c) as Int)) as Boolean)
    }
    catch (`$spock_condition_throwable` : Throwable) {
      org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
        `$spock_valueRecorder`,
        "a + b == c",
        6,
        5,
        null,
        `$spock_condition_throwable`)}
    finally {
    }
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
  }

  @org.spockframework.runtime.model.DataProviderMetadata(line = 0, dataVariables = ["a"], previousDataTableVariables = [])
  fun `$spock_feature_0_0prov0`(): Any {
    return listOf<Int>(1, 2)
  }

  @org.spockframework.runtime.model.DataProviderMetadata(line = 0, dataVariables = ["b"], previousDataTableVariables = ["a"])
  fun `$spock_feature_0_0prov1`(spock_p_a: List<Int>): Any {
    return listOf<Int>(3, 4)
  }

  @org.spockframework.runtime.model.DataProviderMetadata(line = 0, dataVariables = ["c"], previousDataTableVariables = ["a", "b"])
  fun `$spock_feature_0_0prov2`(spock_p_a: List<Int>, spock_p_b: List<Int>): Any {
    return listOf<Int>(4, 6)
  }

  @org.spockframework.runtime.model.DataProcessorMetadata(dataVariables = ["a", "b", "c"])
  fun `$spock_feature_0_0proc`(spock_p0: Any?, spock_p1: Any?, spock_p2: Any?): Any {
    var a: Int
    a = (( spock_p0 ) as Int)
    var b: Int
    b = (( spock_p1 ) as Int)
    var c: Int
    c = (( spock_p2 ) as Int)
    return arrayOf<Any?>(a, b, c)
  }
}
