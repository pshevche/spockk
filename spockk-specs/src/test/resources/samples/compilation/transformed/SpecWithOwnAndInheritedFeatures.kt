@org.spockframework.runtime.model.SpecMetadata(filename = "SpecWithOwnAndInheritedFeatures.kt", line = 1)
abstract class BaseSpec : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "inherited feature 1",
    line = 2,
    parameterNames = [],
    blocks = [org.spockframework.runtime.model.BlockMetadata(
      org.spockframework.runtime.model.BlockKind.EXPECT,
      [""]
    )]
  )
  fun spock_feature_0_0() {
    assert(true)
  }
}

@org.spockframework.runtime.model.SpecMetadata(filename = "SpecWithOwnAndInheritedFeatures.kt", line = 8)
abstract class IntermediateSpec : BaseSpec() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "inherited feature 2",
    line = 9,
    parameterNames = [],
    blocks = [org.spockframework.runtime.model.BlockMetadata(
      org.spockframework.runtime.model.BlockKind.EXPECT,
      [""]
    )]
  )
  fun spock_feature_1_0() {
    assert(true)
  }
}

@org.spockframework.runtime.model.SpecMetadata(filename = "SpecWithOwnAndInheritedFeatures.kt", line = 15)
class Spec : IntermediateSpec() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "own feature",
    line = 16,
    parameterNames = [],
    blocks = [org.spockframework.runtime.model.BlockMetadata(
      org.spockframework.runtime.model.BlockKind.EXPECT,
      [""]
    )]
  )
  fun spock_feature_2_0() {
    assert(true)
  }
}
