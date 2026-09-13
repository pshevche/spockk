interface Counter {
  fun count(): Int
}

@org.spockframework.runtime.model.SpecMetadata(filename = "PrimitiveReturnCardinality.kt", line = 8)
class PrimitiveReturnCardinality : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "some feature",
    line = 9,
    parameterNames = [],
    blocks = [
      org.spockframework.runtime.model.BlockMetadata(
        org.spockframework.runtime.model.BlockKind.SETUP,
        [""]
      ),
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
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    val obj = org.spockframework.runtime.SpecInternals.MockImpl<Counter>(this, "obj", Counter::class.java, Counter::class.java)
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 1)
    ((this.getSpecificationContext() as org.spockframework.runtime.SpecificationContext).getMockController() as org.spockframework.mock.runtime.MockController).enterScope()
    ((this.getSpecificationContext() as org.spockframework.runtime.SpecificationContext).getMockController() as org.spockframework.mock.runtime.MockController).addInteraction(
      org.spockframework.mock.runtime.InteractionBuilder(17, 5, "1 * obj.count() returned 42")
        .setFixedCount(1)
        .addEqualTarget(obj)
        .addEqualMethodName("count")
        .setArgListKind(true, false)
        .addConstantResponse(42)
        .build()
    )
    obj.count()
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 1)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 2)
    ((this.getSpecificationContext() as org.spockframework.runtime.SpecificationContext).getMockController() as org.spockframework.mock.runtime.MockController).leaveScope()
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 2)
  }
}
