@org.spockframework.runtime.model.SpecMetadata(filename = "NoExceptionThrown.kt", line = 1)
class NoExceptionThrown : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "some feature",
    line = 2,
    parameterNames = [],
    blocks = [
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.WHEN,
        [""]
      ),
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.THEN,
        [""]
      )
    ]
  )
  fun `$spock_feature_0_0`() {
    (this.getSpecificationContext() as org.spockframework.runtime.SpecificationContext).setThrownException(null)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    try {
      check(true)
    } catch (`$spock_when_throwable`: Throwable) {
      (this.getSpecificationContext() as org.spockframework.runtime.SpecificationContext).setThrownException(`$spock_when_throwable`)
    } finally {
    }
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 1)
    noExceptionThrown()
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 1)
  }
}
