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

package io.github.pshevche.spockk.smoke.mock

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.Spy
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.variables
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import spock.lang.PendingFeature
import spock.lang.Specification
import kotlin.math.abs
import kotlin.math.pow

interface ISquare {
  fun getLength(): Int

  fun getArea(): Int = getLength() * getLength()
}

interface IDeposit {
  class DepositException(message: String) : Exception(message)

  fun getAnnualNominalInterestRate(): Double

  fun getCompoundingPeriodInMonths(): Double

  fun getCompoundingPeriodsPerYear(): Double = 12 / getCompoundingPeriodInMonths()

  @Throws(DepositException::class)
  fun getBalance(principalAmount: Double, numberOfYears: Double): Double {
    if (principalAmount < 0) throw IllegalArgumentException("Invalid principalAmount: $principalAmount")
    if (numberOfYears < 0) throw DepositException("Invalid numberOfYears: $numberOfYears")
    val j = getAnnualNominalInterestRate()
    val n = getCompoundingPeriodsPerYear()
    return principalAmount * (1 + j / n).pow(n * numberOfYears)
  }

  @Throws(DepositException::class)
  fun getCompoundInterest(principalAmount: Double, numberOfYears: Double): Double =
    getBalance(principalAmount, numberOfYears) - principalAmount
}

interface IQuarterlyCompoundedDeposit : IDeposit {
  override fun getCompoundingPeriodInMonths(): Double = 3.0
}

class PartialMockingInterfacesWithDefaultMethods : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/935")
  @MigratedFrom(
    "org.spockframework.smoke.mock.PartialMockingInterfacesWithDefaultMethods#ISquare area should be computed using the stubbed length - test with when: and then: blocks"
  )
  fun `ISquare area should be computed using the stubbed length - test with when and then blocks`() {
    given
    val square = Spy(ISquare::class.java)
    2 * square.getLength() returns 3

    `when`
    val area = square.getArea()

    then
    area == 9
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/935")
  @MigratedFrom(
    "org.spockframework.smoke.mock.PartialMockingInterfacesWithDefaultMethods#ISquare area should be computed using the stubbed length - test with when:, then: and where: blocks"
  )
  fun `ISquare area should be computed using the stubbed length - test with when, then and where blocks`(
    len: Int,
    ar: Int
  ) {
    given
    val square = Spy(ISquare::class.java)
    2 * square.getLength() returns len

    `when`
    val area = square.getArea()

    then
    area == ar

    where
    len ; ar
    3 ; 9
    5 ; 25
    7 ; 49
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/935")
  @MigratedFrom(
    "org.spockframework.smoke.mock.PartialMockingInterfacesWithDefaultMethods#ISquare area should be computed using the stubbed length - test with when: and then: blocks and various stub values"
  )
  fun `ISquare area should be computed using the stubbed length - test with when and then blocks and various stub values`() {
    given
    val square = Spy(ISquare::class.java)
    2 * square.getLength() returns 3
    2 * square.getLength() returns 5
    2 * square.getLength() returns 7

    `when`
    val area1 = square.getArea()
    val area2 = square.getArea()
    val area3 = square.getArea()

    then
    area1 == 9
    area2 == 25
    area3 == 49
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/935")
  @MigratedFrom(
    "org.spockframework.smoke.mock.PartialMockingInterfacesWithDefaultMethods#ISquare area should be computed using the stubbed length - test with expect: block"
  )
  fun `ISquare area should be computed using the stubbed length - test with expect block`() {
    given
    val square = Spy(ISquare::class.java)
    2 * square.getLength() returns 3

    expect
    square.getArea() == 9
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/935")
  @MigratedFrom(
    "org.spockframework.smoke.mock.PartialMockingInterfacesWithDefaultMethods#ISquare area should be computed using the stubbed length - test with expect: and where: blocks"
  )
  fun `ISquare area should be computed using the stubbed length - test with expect and where blocks`(
    len: Int,
    ar: Int
  ) {
    given
    val square = Spy(ISquare::class.java)
    2 * square.getLength() returns len

    expect
    square.getArea() == ar

    where
    len ; ar
    3 ; 9
    5 ; 25
    7 ; 49
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/935")
  @MigratedFrom(
    "org.spockframework.smoke.mock.PartialMockingInterfacesWithDefaultMethods#ISquare area should be computed using the stubbed length - test with expect: block and various stub values"
  )
  fun `ISquare area should be computed using the stubbed length - test with expect block and various stub values`() {
    given
    val square = Spy(ISquare::class.java)
    2 * square.getLength() returns 3
    2 * square.getLength() returns 5
    2 * square.getLength() returns 7

    expect
    square.getArea() == 9
    square.getArea() == 25
    square.getArea() == 49
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/935")
  @MigratedFrom(
    "org.spockframework.smoke.mock.PartialMockingInterfacesWithDefaultMethods#IQuarterlyCompoundedDeposit: compound interest should be computed using the stubbed annual nominal interest rate"
  )
  fun `IQuarterlyCompoundedDeposit - compound interest should be computed using the stubbed annual nominal interest rate`() {
    given
    val deposit = Spy(IQuarterlyCompoundedDeposit::class.java)
    1 * deposit.getAnnualNominalInterestRate() returns 0.043

    `when`
    val compoundInterest = deposit.getCompoundInterest(1500.0, 6.0)

    then
    abs(compoundInterest - 438.84) <= 0.01
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/935")
  @MigratedFrom(
    "org.spockframework.smoke.mock.PartialMockingInterfacesWithDefaultMethods#IQuarterlyCompoundedDeposit: compound interest should be computed using the stubbed annual nominal interest rate and the stubbed compounding periods per year"
  )
  fun `IQuarterlyCompoundedDeposit - compound interest should be computed using the stubbed annual nominal interest rate and the stubbed compounding periods per year`() {
    given
    val deposit = Spy(IQuarterlyCompoundedDeposit::class.java)
    1 * deposit.getAnnualNominalInterestRate() returns 0.129
    1 * deposit.getCompoundingPeriodsPerYear() returns 12.0

    expect
    abs(deposit.getCompoundInterest(1500.0, 2.0) - 438.84) <= 0.01
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/935")
  @MigratedFrom(
    "org.spockframework.smoke.mock.PartialMockingInterfacesWithDefaultMethods#IQuarterlyCompoundedDeposit: should throw unchecked exception for negative values of principalAmount"
  )
  fun `IQuarterlyCompoundedDeposit - should throw unchecked exception for negative values of principalAmount`() {
    given
    val deposit = Spy(IQuarterlyCompoundedDeposit::class.java)
    deposit.getAnnualNominalInterestRate() returns 0.043

    `when`
    deposit.getCompoundInterest(-1500.0, 6.0)

    then
    thrown(IllegalArgumentException::class.java)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/935")
  @MigratedFrom(
    "org.spockframework.smoke.mock.PartialMockingInterfacesWithDefaultMethods#IQuarterlyCompoundedDeposit: should throw checked exception for negative values of numberOfYears"
  )
  fun `IQuarterlyCompoundedDeposit - should throw checked exception for negative values of numberOfYears`() {
    given
    val deposit = Spy(IQuarterlyCompoundedDeposit::class.java)
    deposit.getAnnualNominalInterestRate() returns 0.043

    `when`
    deposit.getCompoundInterest(1500.0, -6.0)

    then
    thrown(IDeposit.DepositException::class.java)
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.PartialMockingInterfacesWithDefaultMethods#Partial mocking for methods inherited from Object works"
  )
  fun `Partial mocking for methods inherited from Object works`(method: String) {
    given
    val deposit = Spy(IQuarterlyCompoundedDeposit::class.java)

    `when`
    when (method) {
      "toString" -> deposit.toString()
      "hashCode" -> deposit.hashCode()
      else -> deposit.equals(null)
    }

    then
    noExceptionThrown()

    where
    variable(method).from("toString", "hashCode", "equals")
  }
}
