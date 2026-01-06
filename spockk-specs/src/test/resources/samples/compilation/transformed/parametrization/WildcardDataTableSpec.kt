@org.spockframework.runtime.model.SpecMetadata(filename = "WildcardDataTableSpec.kt", line = 1)
class WildcardDataTableSpec : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "can use pseudo-column to enable one-column table",
    line = 2,
    parameterNames = ["a"],
    blocks = [org.spockframework.runtime.model.BlockMetadata(
      org.spockframework.runtime.model.BlockKind.EXPECT,
      [""]
    ),
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.WHERE,
        [""]
      )]
  )
  fun `$spock_feature_0_0`(a: Int) {
    kotlin.test.assertEquals(1, a)
  }

  @org.spockframework.runtime.model.DataProviderMetadata(line = 0,
    dataVariables = ["a"],
    previousDataTableVariables = [])
  fun `$spock_feature_0_0prov0`(): Any {
    return listOf<Int>(1)
  }

  @org.spockframework.runtime.model.DataProcessorMetadata(dataVariables = ["a"])
  fun `$spock_feature_0_0proc`(spock_p0: Any): Any {
    var a: Int
    a = (( spock_p0 ) as Int)
    return arrayOf<Any>(a)
  }

  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 1,
    name = "pseudo-column can be declared as parameter",
    line = 11,
    parameterNames = ["a", "_"],
    blocks = [org.spockframework.runtime.model.BlockMetadata(
      org.spockframework.runtime.model.BlockKind.EXPECT,
      [""]
    ),
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.WHERE,
        [""]
      )]
  )
  fun `$spock_feature_0_1`(a: Int, `_`: Any) {
    kotlin.test.assertEquals(3, a)
  }

  @org.spockframework.runtime.model.DataProviderMetadata(line = 0,
    dataVariables = ["a"],
    previousDataTableVariables = [])
  fun `$spock_feature_0_1prov0`(): Any {
    return listOf<Int>(3)
  }

  @org.spockframework.runtime.model.DataProviderMetadata(line = 0,
    dataVariables = ["_"],
    previousDataTableVariables = ["a"])
  fun `$spock_feature_0_1prov1`(spock_p_a: List<Any>): Any {
    return listOf<Any>(`_`)
  }

  @org.spockframework.runtime.model.DataProcessorMetadata(dataVariables = ["a", "_"])
  fun `$spock_feature_0_1proc`(spock_p0: Any, spock_p1: Any): Any {
    var a: Int
    a = (( spock_p0 ) as Int)
    var `_`: Any
    `_` = (( spock_p1 ) as Any)
    return arrayOf<Any>(a, `_`)
  }
}

