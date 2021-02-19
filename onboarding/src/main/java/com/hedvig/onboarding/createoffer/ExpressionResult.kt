package com.hedvig.onboarding.createoffer

sealed class ExpressionResult {
    data class True(
        val resultValue: String?,
    ) : ExpressionResult()

    object False : ExpressionResult()
}
