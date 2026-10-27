interface IntList {
  fun add(element: Int): Boolean
}

@org.spockframework.runtime.model.SpecMetadata(filename = "SimpleInteraction.kt", line = 7)
class SimpleInteraction : spock.lang.Specification() {
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
    val `$spock_valueRecorder` : org.spockframework.runtime.ValueRecorder = org.spockframework.runtime.ValueRecorder()
    val `$spock_errorCollector` : org.spockframework.runtime.ErrorCollector =
      org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    val list = org.spockframework.runtime.SpecInternals.MockImpl<IntList>(this, "list", IntList::class.java, IntList::class.java)
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 1)
    ((this.getSpecificationContext() as org.spockframework.runtime.SpecificationContext).getMockController() as org.spockframework.mock.runtime.MockController).enterScope()
    ((this.getSpecificationContext() as org.spockframework.runtime.SpecificationContext).getMockController() as org.spockframework.mock.runtime.MockController).addInteraction(
      org.spockframework.mock.runtime.InteractionBuilder(16, 5, "1 * list.add(1)")
        .setFixedCount(1)
        .addEqualTarget(list)
        .addEqualMethodName("add")
        .setArgListKind(true, false)
        .addEqualArg(1)
        .build()
    )
    list.add(1)
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 1)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 2)
    ((this.getSpecificationContext() as org.spockframework.runtime.SpecificationContext).getMockController() as org.spockframework.mock.runtime.MockController).leaveScope()
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 2)
  }
}
