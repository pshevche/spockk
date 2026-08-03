@org.spockframework.runtime.model.SpecMetadata(filename = "VerifyInExtensionFunction.kt", line = 1)
class VerifyInExtensionFunction : spock.lang.Specification() {
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
}

fun spock.lang.Specification.checkValue() {
  io.github.pshevche.spockk.lang.verify(1) { this == 1 }
}
