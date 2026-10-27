@org.spockframework.runtime.model.SpecMetadata(filename = "ConditionMethodVerifyAllWithOnlyException.kt", line = 1)
class ConditionMethodVerifyAllWithOnlyException : spock.lang.Specification() {
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
    io.github.pshevche.spockk.lang.verifyAll(listOf("")) {
      val `$spock_verifyAllErrorCollector` : org.spockframework.runtime.ErrorCollector = org.spockframework.runtime.ErrorCollector()
      throw java.lang.Exception("foo")
      `$spock_verifyAllErrorCollector`.validateCollectedErrors()
    }
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
  }
}
