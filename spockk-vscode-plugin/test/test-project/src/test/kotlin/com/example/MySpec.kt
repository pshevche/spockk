package com.example

import io.github.pshevche.spockk.lang.*

class MySpec {
  fun `passing feature`() {
    expect: 1 == 1
  }

  fun `failing feature`() {
    expect: 1 == 2
  }
}
