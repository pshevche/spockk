package io.github.pshevche.spockk.compilation.common

import org.jetbrains.kotlin.ir.IrStatement

internal data class FeatureBlockStatements(
    val label: FeatureBlockLabel,
    val description: String,
    val statements: List<IrStatement>,
)
