package org.spockframework.shadow

// Mirrors a real quirk of the upstream tree: spock-specs checks in the actual source of its own
// EmbeddedSpecification base class, right alongside the specs that extend it. A chain-walk that
// doesn't treat framework root names as always-terminal would resolve straight through this class
// definition and lose the classification signal several layers further up.
abstract class EmbeddedSpecification extends Specification {
}

class UsesShadowedEmbeddedSpecification extends EmbeddedSpecification {
  def "feature on a direct subclass of the shadowed root"() {
    expect: true
  }
}
