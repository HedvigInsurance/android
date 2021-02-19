package com.hedvig.onboarding.createoffer.tooltip

sealed class TooltipModel {
    data class Header(val text: String? = null) : TooltipModel()
    sealed class Tooltip : TooltipModel() {
        data class TooltipWithTitle(val title: String, val description: String) : TooltipModel()
        data class TooltipWithOutTitle(val description: String) : TooltipModel()
    }
}
