@org.spockframework.runtime.model.SpecMetadata(filename = "CleanupBlock.kt", line = 1)
class CleanupBlock : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "some feature",
    line = 2,
    parameterNames = [],
    blocks = [
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.EXPECT,
        [""]
      ),
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.CLEANUP,
        [""]
      )
    ]
  )
  fun `$spock_feature_0_0`() {
    var `$spock_feature_throwable`: Throwable? = null
    try {
      assert(true)
    } catch (`$spock_tmp_throwable`: Throwable) {
      `$spock_feature_throwable` = `$spock_tmp_throwable`
      throw `$spock_tmp_throwable`
    } finally {
      try {
        println("cleanup")
      } catch (`$spock_tmp_throwable`: Throwable) {
        if (`$spock_feature_throwable` != null) {
          `$spock_feature_throwable`.addSuppressed(`$spock_tmp_throwable`)
        } else {
          throw `$spock_tmp_throwable`
        }
      }
    }
  }
}
