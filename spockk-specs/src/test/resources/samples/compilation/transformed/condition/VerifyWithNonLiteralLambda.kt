@org.spockframework.runtime.model.SpecMetadata(filename = "VerifyWithNonLiteralLambda.kt", line = 1)
class VerifyWithNonLiteralLambda : spock.lang.Specification() {
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
    val block: Int.() -> Unit = { this == 1 }
    io.github.pshevche.spockk.lang.verify(1, block)
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
  }
}
