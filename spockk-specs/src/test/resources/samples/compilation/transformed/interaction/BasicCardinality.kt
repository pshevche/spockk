interface Greeter {
  fun greet(name: String)
}

@org.spockframework.runtime.model.SpecMetadata(filename = "BasicCardinality.kt", line = 7)
class BasicCardinality : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(
    ordinal = 0,
    name = "some feature",
    line = 8,
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
    val obj = org.spockframework.runtime.SpecInternals.MockImpl<Greeter>(this, "obj", Greeter::class.java, Greeter::class.java)
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 1)
    ((this.getSpecificationContext() as org.spockframework.runtime.SpecificationContext).getMockController() as org.spockframework.mock.runtime.MockController).enterScope()
    ((this.getSpecificationContext() as org.spockframework.runtime.SpecificationContext).getMockController() as org.spockframework.mock.runtime.MockController).addInteraction(
      org.spockframework.mock.runtime.InteractionBuilder(16, 5, "1 * obj.greet(\"Alice\")")
        .setFixedCount(1)
        .addEqualTarget(obj)
        .addEqualMethodName("greet")
        .setArgListKind(true, false)
        .addEqualArg("Alice")
        .build()
    )
    obj.greet("Alice")
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 1)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 2)
    ((this.getSpecificationContext() as org.spockframework.runtime.SpecificationContext).getMockController() as org.spockframework.mock.runtime.MockController).leaveScope()
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 2)
  }
}
