@org.spockframework.runtime.model.SpecMetadata(filename = "SharedLateinitVarField.kt", line = 1)
class SharedLateinitVarField : spock.lang.Specification() {
  @spock.lang.Shared
  @org.spockframework.runtime.model.FieldMetadata(
    name = "uninitializedSharedField",
    ordinal = 0,
    line = 3,
    initializer = false
  )
  @kotlin.jvm.Volatile
  protected lateinit var `$spock_sharedField_uninitializedSharedField`: String

  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "some feature",
    line = 5,
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
    setUninitializedSharedField("initialized")
    try {
      org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
        `$spock_valueRecorder`.reset(),
        "(uninitializedSharedField == \"initialized\")",
        8,
        5,
        null,
        `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2),
          (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(0), getUninitializedSharedField()) as String) == (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), "initialized") as String)) as Boolean)
    }
    catch (`$spock_condition_throwable` : Throwable) {
      org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
        `$spock_valueRecorder`,
        "(uninitializedSharedField == \"initialized\")",
        8,
        5,
        null,
        `$spock_condition_throwable`)}
    finally {
    }
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
  }

  fun getUninitializedSharedField(): String =
    ((specificationContext as org.spockframework.runtime.SpecificationContext).sharedInstance as SharedLateinitVarField).`$spock_sharedField_uninitializedSharedField`

  fun setUninitializedSharedField(value: String) {
    ((specificationContext as org.spockframework.runtime.SpecificationContext).sharedInstance as SharedLateinitVarField).`$spock_sharedField_uninitializedSharedField` =
      value
  }
}
