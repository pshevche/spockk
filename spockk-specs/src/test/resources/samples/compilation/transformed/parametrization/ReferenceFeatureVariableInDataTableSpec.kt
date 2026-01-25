import io.github.pshevche.spockk.lang.*
import kotlin.test.assertEquals

@org.spockframework.runtime.model.SpecMetadata(filename = "ReferenceFeatureVariableInDataTableSpec.kt", line = 4)
class ReferenceFeatureVariableInDataTableSpec : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "a feature",
    line = 5,
    parameterNames = ["a", "b"],
    blocks = [org.spockframework.runtime.model.BlockMetadata(
      org.spockframework.runtime.model.BlockKind.EXPECT,
      [""]
    ),
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.WHERE,
        [""]
      )]
  )
  fun `$spock_feature_0_0`(a: Int, b: Int) {
    assertEquals(b, a)
  }

  @org.spockframework.runtime.model.DataProviderMetadata(line = 0,
    dataVariables = ["a"],
    previousDataTableVariables = [])
  fun `$spock_feature_0_0prov0`(): Any {
    return listOf<Int>(1, 2, 3)
  }

  @org.spockframework.runtime.model.DataProviderMetadata(line = 0,
    dataVariables = ["b"],
    previousDataTableVariables = ["a"])
  fun `$spock_feature_0_0prov1`(spock_p_a: List<Int>): Any {
    return listOf<Int>(1, spock_p_a.get(1), 3)
  }

  @org.spockframework.runtime.model.DataProcessorMetadata(dataVariables = ["a", "b"])
  fun `$spock_feature_0_0proc`(spock_p0: Any, spock_p1: Any): Any {
    var a: Int
    a = (( spock_p0 ) as Int)
    var b: Int
    b = (( spock_p1 ) as Int)
    return arrayOf<Any>(a, b)
  }
}
