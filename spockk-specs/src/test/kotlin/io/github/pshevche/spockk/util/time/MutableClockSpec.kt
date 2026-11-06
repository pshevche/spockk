/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.pshevche.spockk.util.time

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll
import spock.util.time.MutableClock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.temporal.TemporalAmount

class MutableClockSpec : Specification() {

  companion object {
    private val defaultTime: ZonedDateTime = ZonedDateTime.of(2000, 6, 5, 12, 30, 30, 100, UTC)
    private val defaultInstant: Instant = defaultTime.toInstant()
  }

  @Subject
  private var mutableClock = MutableClock(defaultTime)

  @MigratedFrom("spock.util.time.MutableClockSpec#MutableClock(ZonedDateTime) uses instant and zone from ZonedDateTime")
  fun `MutableClock(ZonedDateTime) uses instant and zone from ZonedDateTime`() {
    expect
    mutableClock.instant() == defaultInstant
    mutableClock.zone == defaultTime.zone
  }

  @MigratedFrom("spock.util.time.MutableClockSpec#MutableClock() initialized with current time and default zone")
  fun `MutableClock() initialized with current time and default zone`() {
    given
    val start = Instant.now()
    mutableClock = MutableClock()
    val end = Instant.now()

    expect
    !mutableClock.instant().isBefore(start)
    !mutableClock.instant().isAfter(end)
    mutableClock.zone == ZoneId.systemDefault()
  }

  @MigratedFrom("spock.util.time.MutableClockSpec#MutableClock(ZoneId) initialized with current time and given zone")
  fun `MutableClock(ZoneId) initialized with current time and given zone`() {
    given
    val zoneId = ZoneId.of("CET")
    val start = Instant.now()
    mutableClock = MutableClock(zoneId)
    val end = Instant.now()

    expect
    !mutableClock.instant().isBefore(start)
    !mutableClock.instant().isAfter(end)
    mutableClock.zone == zoneId
  }

  @MigratedFrom("spock.util.time.MutableClockSpec#MutableClock(Instant) initialized with default zone")
  fun `MutableClock(Instant) initialized with default zone`() {
    given
    mutableClock = MutableClock(defaultInstant)

    expect
    mutableClock.instant() == defaultInstant
    mutableClock.zone == ZoneId.systemDefault()
  }

  @MigratedFrom(
    "spock.util.time.MutableClockSpec#MutableClock.withZone(ZoneId) returns a new instance with different zone"
  )
  fun `MutableClock withZone(ZoneId) returns a new instance with different zone`() {
    given
    val other = mutableClock.withZone(ZoneId.of("CET"))

    expect
    mutableClock !== other
    other.zone == ZoneId.of("CET")
    mutableClock.instant() == other.instant()
  }

  @Unroll("MutableClock(#instant, #zone, #changeAmount) throws AssertionError")
  @MigratedFrom("spock.util.time.MutableClockSpec#MutableClock may not be initialized with null values")
  fun `MutableClock may not be initialized with null values`(
    instant: Instant?,
    zone: ZoneId?,
    changeAmount: TemporalAmount?
  ) {
    `when`
    MutableClock(instant, zone, changeAmount)

    then
    thrown(NullPointerException::class.java)

    where
    instant         ; zone ; changeAmount
    null            ; UTC  ; Duration.ofSeconds(1)
    defaultInstant  ; null ; Duration.ofSeconds(1)
    null            ; null ; Duration.ofSeconds(1)
    defaultInstant  ; UTC  ; null
    null            ; UTC  ; null
    defaultInstant  ; null ; null
    null            ; null ; null
  }

  @MigratedFrom("spock.util.time.MutableClockSpec#MutableClock to String includes fields")
  fun `MutableClock to String includes fields`() {
    expect
    mutableClock.toString() == "MutableClock{zone=Z, instant=2000-06-05T12:30:30.000000100Z, changeAmount=PT1S}"
  }

  @MigratedFrom("spock.util.time.MutableClockSpec#MutableClock instant can't be null")
  fun `MutableClock instant can't be null`() {
    `when`
    mutableClock.setInstant(null)

    then
    thrown(NullPointerException::class.java)
  }

  @MigratedFrom("spock.util.time.MutableClockSpec#changeAmount can't be null")
  fun `changeAmount can't be null`() {
    `when`
    mutableClock.setChangeAmount(null)

    then
    thrown(NullPointerException::class.java)
  }
}
