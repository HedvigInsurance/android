package com.hedvig.onboarding.embark

sealed class ExpressionResult {
    data class True(
        val resultValue: String?,
    ) : ExpressionResult()

    object False : ExpressionResult()
}
