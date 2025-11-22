package io.github.pshevche.spockk.fixtures.runtime

import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.testkit.engine.EngineTestKit
import org.junit.platform.testkit.engine.Events

object EngineTestKitUtils {
    fun execute(selector: DiscoverySelector): Events {
        return EngineTestKit.engine("spock")
            .selectors(selector)
            .execute()
            .testEvents()
            .debug()
    }
}
