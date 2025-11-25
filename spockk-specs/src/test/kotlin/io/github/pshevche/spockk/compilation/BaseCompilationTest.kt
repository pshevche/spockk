package io.github.pshevche.spockk.compilation

import io.github.pshevche.spockk.fixtures.compilation.CompilationUtils.compile
import io.github.pshevche.spockk.fixtures.compilation.CompilationUtils.transform
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import spock.lang.Specification

@OptIn(ExperimentalCompilerApi::class)
abstract class BaseCompilationTest : Specification() {

    protected fun assertTransformation(sample: TransformationSample) {
        val actual = transform(sample.source)
        val expected = compile(sample.expected)
        val aDump = actual.irDump
        val eDump = expected.irDump

        assert(actual.isSuccess() && expected.isSuccess())
        assert(aDump == eDump)
    }
}
