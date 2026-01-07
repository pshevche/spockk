import io.github.pshevche.spockk.lang.*
import kotlin.test.assertEquals

@org.spockframework.runtime.model.SpecMetadata(filename = "MultiPipeMultiVariableSpec.kt", line = 4)
class MultiPipeMultiVariableSpec : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "multiple data pipes",
    line = 5,
    parameterNames = ["a", "b", "c"],
    blocks = [org.spockframework.runtime.model.BlockMetadata(
      org.spockframework.runtime.model.BlockKind.EXPECT,
      [""]
    ),
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.WHERE,
        [""]
      )]
  )
  fun `$spock_feature_0_0`(a: Int, b: Int, c: Int) {
    assertEquals(c, a + b)
  }

  @org.spockframework.runtime.model.DataProviderMetadata(line = 0, dataVariables = ["a"], previousDataTableVariables = [])
  fun `$spock_feature_0_0prov0`(): Any {
    return listOf<Int>(1, 2)
  }

  @org.spockframework.runtime.model.DataProviderMetadata(line = 0, dataVariables = ["b"], previousDataTableVariables = ["a"])
  fun `$spock_feature_0_0prov1`(spock_p_a: List<Any>): Any {
    return listOf<Int>(3, 4)
  }

  @org.spockframework.runtime.model.DataProviderMetadata(line = 0, dataVariables = ["c"], previousDataTableVariables = ["a", "b"])
  fun `$spock_feature_0_0prov2`(spock_p_a: List<Any>, spock_p_b: List<Any>): Any {
    return listOf<Int>(4, 6)
  }

  @org.spockframework.runtime.model.DataProcessorMetadata(dataVariables = ["a", "b", "c"])
  fun `$spock_feature_0_0proc`(spock_p0: Any, spock_p1: Any, spock_p2: Any): Any {
    var a: Int
    a = (( spock_p0 ) as Int)
    var b: Int
    b = (( spock_p1 ) as Int)
    var c: Int
    c = (( spock_p2 ) as Int)
    return arrayOf<Any>(a, b, c)
  }
}
