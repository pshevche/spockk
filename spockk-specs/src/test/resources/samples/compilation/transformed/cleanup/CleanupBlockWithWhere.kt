@org.spockframework.runtime.model.SpecMetadata(filename = "CleanupBlockWithWhere.kt", line = 1)
class CleanupBlockWithWhere : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "some feature",
    line = 2,
    parameterNames = ["a"],
    blocks = [
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.EXPECT,
        [""]
      ),
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.CLEANUP,
        [""]
      ),
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.WHERE,
        [""]
      )
    ]
  )
  fun `$spock_feature_0_0`(a: Int) {
    var `$spock_feature_throwable`: Throwable? = null
    try {
      assert(a > 0)
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

  @org.spockframework.runtime.model.DataProviderMetadata(line = 0, dataVariables = ["a"], previousDataTableVariables = [])
  fun `$spock_feature_0_0prov0`(): Any {
    return listOf<Int>(1, 2, 3)
  }

  @org.spockframework.runtime.model.DataProcessorMetadata(dataVariables = ["a"])
  fun `$spock_feature_0_0proc`(spock_p0: Any): Any {
    var a: Int
    a = ((spock_p0) as Int)
    return arrayOf<Any>(a)
  }
}
