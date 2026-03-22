import io.github.pshevche.spockk.lang.*
import kotlin.test.assertEquals

@org.spockframework.runtime.model.SpecMetadata(filename = "SingleVariableVarargValuesSpec.kt", line = 4)
class SingleVariableVarargValuesSpec : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "single variable vararg values",
    line = 5,
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
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    assertEquals(0, a % 2)
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
  }

  @org.spockframework.runtime.model.DataProviderMetadata(line = 0, dataVariables = ["a"], previousDataTableVariables = [])
  fun `$spock_feature_0_0prov0`(): Any {
    return listOf<Int>(2, 4, 6)
  }

  @org.spockframework.runtime.model.DataProcessorMetadata(dataVariables = ["a"])
  fun `$spock_feature_0_0proc`(spock_p0: Any): Any {
    var a: Int
    a = (( spock_p0 ) as Int)
    return arrayOf<Any>(a)
  }
}
