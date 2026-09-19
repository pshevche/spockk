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

  @PendingFeature(reason = "assert() in a when block isn't rewritten yet, see #326")
  @MigratedFrom("org.spockframework.smoke.C#nested parens in reason")
  fun `has parens inside its pending reason`() {}
}
