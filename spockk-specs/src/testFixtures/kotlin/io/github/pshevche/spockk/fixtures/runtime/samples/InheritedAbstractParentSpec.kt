package io.github.pshevche.spockk.fixtures.runtime.samples

import io.github.pshevche.spockk.lang.expect
import spock.lang.Specification

abstract class InheritedAbstractParentSpec : Specification() {

    fun `successful parent feature`() {
        expect
        assert(true)
    }

    fun `failing parent feature`() {
        expect
        assert(false)
    }
}
