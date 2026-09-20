package org.spockframework.smoke.condition

import org.spockframework.EmbeddedSpecification
import spock.lang.Snapshot

class SimpleConditions extends Specification {
  def "plain feature"() {
    expect: 1 == 1
  }

  def 'single quoted feature'() {
    expect: true
  }

  def "feature with #placeholder and, punctuation"() {
    expect: x
    where: x << [true]
  }

  // not a feature: helper method
  private def helper() { 42 }
}

abstract class AbstractWithNoSubclass extends Specification {
  def "orphaned abstract feature"() {
    expect: true
  }
}

abstract class AbstractWithFeature extends Specification {
  def "feature declared in the abstract base"() {
    expect: true
  }
}

class ConcreteFromAbstractBase extends AbstractWithFeature {
  def "concrete class's own feature"() {
    expect: true
  }
}

class ConcreteWithNoOwnFeatures extends AbstractWithFeature {
  // no def "..." methods at all: inherits its entire feature set from the abstract base
  private String helperOnly() { "not a feature" }
}

class EmbeddedConditions extends EmbeddedSpecification {
  def "runs an embedded spec"() {
    when:
    def result = runner.runSpecBody("...")
    then:
    result.totalFailureCount == 0
  }

  def "compiles a fixture spec from an embedded string"() {
    when:
    def result = runner.runWithImports("""
class Foo extends Specification {
  def "embedded feature"() {
    expect: true
  }
}
class Bar extends Foo {
}
    """)
    then:
    result.testsSucceededCount == 1
  }
}

class AfterEmbeddedFixture extends Specification {
  def "real class parsed correctly after a masked embedded string"() {
    expect: true
  }
}
