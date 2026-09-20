package io.github.pshevche.spockk.smoke.sample

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import spock.lang.PendingFeature

class PortedTest : Specification() {
  @MigratedFrom("org.spockframework.smoke.A#one")
  fun `covers one feature`() {}

  @MigratedFrom("org.spockframework.smoke.A#two", "org.spockframework.smoke.A#three")
  fun `covers two features`() {}

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/412")
  @MigratedFrom("org.spockframework.smoke.B#blocked")
  fun `is pending`() {}

  @MigratedFrom("org.spockframework.smoke.C#each condition gets its own values (no undesired aliasing)")
  fun `key with parens is still parsed correctly`() {}
}
