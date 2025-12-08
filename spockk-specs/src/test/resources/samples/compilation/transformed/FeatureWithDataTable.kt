@org.spockframework.runtime.model.SpecMetadata(filename = "FeatureWithDataTable.kt", line = 1)
class FeatureWithDataTable : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "some feature",
    line = 2,
    parameterNames = ["a", "b", "c"],
    blocks = [
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.EXPECT,
        [""]
      ),
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.WHERE,
        [""]
      )
    ]
  )
  fun spock_feature_0_0(a: Int, b: Int, c: Int) {
    assert(a + b == c)
  }
}
