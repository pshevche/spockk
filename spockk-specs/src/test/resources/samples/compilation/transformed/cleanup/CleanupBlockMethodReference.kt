@org.spockframework.runtime.model.SpecMetadata(filename = "CleanupBlockMethodReference.kt", line = 1)
class CleanupBlockMethodReference : spock.lang.Specification() {
  @Suppress("UNINITIALIZED_VARIABLE")
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "some feature",
    line = 3,
    parameterNames = [],
    blocks = [
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.WHEN,
        [""]
      ),
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.THEN,
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
    val foobar: String
    try {
      org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
      foobar = foobar()
      org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
      org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 1)
      println(foobar)
      org.spockframework.runtime.SpockRuntime.callBlockExited(this, 1)
    } catch (`$spock_tmp_throwable`: Throwable) {
      `$spock_feature_throwable` = `$spock_tmp_throwable`
      throw `$spock_tmp_throwable`
    } finally {
      var `$spock_failedBlock`: org.spockframework.runtime.model.BlockInfo? = null
      try {
        if (`$spock_feature_throwable` != null) {
          `$spock_failedBlock` = (this.getSpecificationContext() as org.spockframework.runtime.SpecificationContext).getCurrentBlock()
        }
        org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 2)
        foobar.length
        org.spockframework.runtime.SpockRuntime.callBlockExited(this, 2)
      } catch (`$spock_tmp_throwable`: Throwable) {
        if (`$spock_feature_throwable` != null) {
          `$spock_feature_throwable`.addSuppressed(`$spock_tmp_throwable`)
        } else {
          throw `$spock_tmp_throwable`
        }
      } finally {
        if (`$spock_feature_throwable` != null) {
          (this.getSpecificationContext() as org.spockframework.runtime.SpecificationContext).setCurrentBlock(`$spock_failedBlock`)
        }
      }
    }
  }

  fun foobar(): String = "foo"
}
