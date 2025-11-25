package io.github.pshevche.spockk.runtime

import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.InheritedAbstractParentSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.InheritedFromAbstractChildSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.InheritedOpenChildSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.InheritedOpenParentSpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod
import spock.lang.Specification

class SpockkTestEngineInheritanceTest : Specification() {

    fun `discovers feature methods defined both in parent and child`() {
        `when`
        val events = execute(selectClass(InheritedOpenChildSpec::class.java))

        then
        events.assertStatistics {
            it.started(4).succeeded(2).failed(2)
        }
    }

    fun `discovers class with features declared only in the parent class`() {
        `when`
        val events = execute(selectClass(InheritedFromAbstractChildSpec::class.java))

        then
        events.assertStatistics {
            it.started(2).succeeded(1).failed(1)
        }
    }

    fun `discovers feature methods by name from parent class`() {
        `when`
        val events = execute(selectMethod(InheritedOpenChildSpec::class.java, "successful parent feature"))

        then
        events.assertStatistics {
            it.started(1).succeeded(1)
        }
    }

    fun `discovers tests in open classes`() {
        `when`
        val events = execute(selectClass(InheritedOpenParentSpec::class.java))

        then
        events.assertStatistics { it.started(2) }
    }

    fun `does not discover tests in abstract classes`() {
        `when`
        val events = execute(selectClass(InheritedAbstractParentSpec::class.java))

        then
        events.assertStatistics { it.started(0) }
    }

    fun `executes inherited features before own features`() {
        `when`
        val features = execute(selectClass(InheritedOpenChildSpec::class.java))
            .started()
            .map { it.testDescriptor.displayName }
            .toList()

        then
        assert(
            features == listOf(
                "successful parent feature",
                "failing parent feature",
                "successful child feature",
                "failing child feature"
            )
        )
    }

}
