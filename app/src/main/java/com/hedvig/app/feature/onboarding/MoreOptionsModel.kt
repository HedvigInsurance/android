package com.hedvig.app.feature.onboarding

sealed class MoreOptionsModel {
    object Header : MoreOptionsModel()
    sealed class Row : MoreOptionsModel() {
        sealed class UserId : Row() {
            data class Success(val id: String) : UserId()
            object Error : UserId()
        }

        object Version : Row()
        object Settings : Row()
    }

    object Copyright : MoreOptionsModel()
}
