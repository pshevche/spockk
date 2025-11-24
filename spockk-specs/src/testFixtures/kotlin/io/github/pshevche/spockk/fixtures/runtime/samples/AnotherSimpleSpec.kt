package io.github.pshevche.spockk.fixtures.runtime.samples

import io.github.pshevche.spockk.lang.expect
import spock.lang.Specification

class AnotherSimpleSpec : Specification() {

    fun `successful feature`() {
        expect
        assert(true)
    }

    fun `failing feature`() {
        expect
        assert(false)
    }
}
