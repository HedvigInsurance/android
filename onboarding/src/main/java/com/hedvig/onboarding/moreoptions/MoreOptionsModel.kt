package com.hedvig.onboarding.moreoptions

sealed class MoreOptionsModel {
    object Header : MoreOptionsModel()
    sealed class UserId : MoreOptionsModel() {
        data class Success(val id: String) : UserId()
        object Error : UserId()
    }

    object Version : MoreOptionsModel()
    object Settings : MoreOptionsModel()
    object Copyright : MoreOptionsModel()
}
