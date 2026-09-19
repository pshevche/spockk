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

abstract class AbstractBase extends Specification {
  def "abstract classes are skipped entirely"() {
    expect: true
  }
}

class EmbeddedConditions extends EmbeddedSpecification {
  def "runs an embedded spec"() {
    when:
    def result = runner.runSpecBody("...")
    then:
    result.totalFailureCount == 0
  }
}
