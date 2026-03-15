@org.spockframework.runtime.model.SpecMetadata(filename = "CleanupSpecMethod.kt", line = 1)
class CleanupSpecMethod : spock.lang.Specification() {
  private fun cleanupSpec() {
    println("cleanupSpec")
  }

  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "some feature",
    line = 6,
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
    (this.getSpecificationContext().getMockController() as org.spockframework.mock.runtime.MockController).leaveScope()
  }
}
