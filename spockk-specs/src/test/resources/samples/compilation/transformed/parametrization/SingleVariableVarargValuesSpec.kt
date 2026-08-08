import io.github.pshevche.spockk.lang.*

@org.spockframework.runtime.model.SpecMetadata(filename = "SingleVariableVarargValuesSpec.kt", line = 3)
class SingleVariableVarargValuesSpec : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "single variable vararg values",
    line = 4,
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
        "a % 2 == 0",
        6,
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
    return listOf<Int>(2, 4, 6)
  }

  @org.spockframework.runtime.model.DataProcessorMetadata(dataVariables = ["a"])
  fun `$spock_feature_0_0proc`(spock_p0: Any): Any {
    var a: Int
    a = (( spock_p0 ) as Int)
    return arrayOf<Any>(a)
  }
}
