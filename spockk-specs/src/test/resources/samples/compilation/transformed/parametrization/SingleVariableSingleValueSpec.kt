import io.github.pshevche.spockk.lang.*
import kotlin.test.assertEquals

@org.spockframework.runtime.model.SpecMetadata(filename = "SingleVariableSingleValueSpec.kt", line = 4)
class SingleVariableSingleValueSpec : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "single variable single value",
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
    assertEquals(1, a)
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
  }

  @org.spockframework.runtime.model.DataProcessorMetadata(dataVariables = ["a"])
  fun `$spock_feature_0_0proc`(): Any {
    var a: Int
    a = (( 1 ) as Int)
    return arrayOf<Any>(a)
  }
}
