import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.where

class Spec {

  fun `parameterized feature`(variable1: String, variable2: String) {
    expect
    throw RuntimeException("Boom!")

    where
    variable1 ; variable2
    "val11"   ; "val21"
  }
}
