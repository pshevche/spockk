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
      val `$spock_valueRecorder` : org.spockframework.runtime.ValueRecorder = org.spockframework.runtime.ValueRecorder()
      val `$spock_errorCollector` : org.spockframework.runtime.ErrorCollector =
        org.spockframework.runtime.ErrorRethrower.INSTANCE
      org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
      try {
        org.spockframework.runtime.SpockRuntime.verifyCondition(`$spock_errorCollector`,
          `$spock_valueRecorder`.reset(),
          "(a > 0)",
          4,
          5,
          null,
          `$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(2),
            (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(0), a) as Int) > (`$spock_valueRecorder`.record(`$spock_valueRecorder`.startRecordingValue(1), 0) as Int)) as Boolean)
      }
      catch (`$spock_condition_throwable` : Throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException(`$spock_errorCollector`,
          `$spock_valueRecorder`,
          "(a > 0)",
          4,
          5,
          null,
          `$spock_condition_throwable`)}
      finally {
      }
      org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
    } catch (`$spock_tmp_throwable`: Throwable) {
      `$spock_feature_throwable` = `$spock_tmp_throwable`
      throw `$spock_tmp_throwable`
    } finally {
      var `$spock_failedBlock`: org.spockframework.runtime.model.BlockInfo? = null
      try {
        if (`$spock_feature_throwable` != null) {
          `$spock_failedBlock` = (this.getSpecificationContext() as org.spockframework.runtime.SpecificationContext).getCurrentBlock()
        }
        org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 1)
        println("cleanup")
        org.spockframework.runtime.SpockRuntime.callBlockExited(this, 1)
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

  @org.spockframework.runtime.model.DataProviderMetadata(line = 0, dataVariables = ["a"], previousDataTableVariables = [])
  fun `$spock_feature_0_0prov0`(): Any {
    return listOf<Int>(1, 2, 3)
  }

  @org.spockframework.runtime.model.DataProcessorMetadata(dataVariables = ["a"])
  fun `$spock_feature_0_0proc`(spock_p0: Any?): Any {
    var a: Int
    a = ((spock_p0) as Int)
    return arrayOf<Any?>(a)
  }
}
