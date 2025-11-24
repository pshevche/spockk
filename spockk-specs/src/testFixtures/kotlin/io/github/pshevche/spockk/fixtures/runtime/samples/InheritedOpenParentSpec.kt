package io.github.pshevche.spockk.fixtures.runtime.samples

import io.github.pshevche.spockk.lang.expect
import spock.lang.Specification

open class InheritedOpenParentSpec : Specification() {

    fun `successful parent feature`() {
        expect
        assert(true)
    }

    fun `failing parent feature`() {
        expect
        assert(false)
    }
}
