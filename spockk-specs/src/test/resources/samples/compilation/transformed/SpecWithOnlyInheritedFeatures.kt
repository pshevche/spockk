@org.spockframework.runtime.model.SpecMetadata(filename = "SpecWithOnlyInheritedFeatures.kt", line = 1)
abstract class BaseSpec : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "inherited feature",
    line = 2,
    parameterNames = [],
    blocks = [org.spockframework.runtime.model.BlockMetadata(
      org.spockframework.runtime.model.BlockKind.EXPECT,
      [""]
    )]
  )
  fun `$spock_feature_0_0`() {
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    assert(true)
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
  }
}

@org.spockframework.runtime.model.SpecMetadata(filename = "SpecWithOnlyInheritedFeatures.kt", line = 8)
class Spec : BaseSpec()
