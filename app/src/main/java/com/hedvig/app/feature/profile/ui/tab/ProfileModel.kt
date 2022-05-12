package com.hedvig.app.feature.profile.ui.tab

import androidx.annotation.DrawableRes

sealed class ProfileModel {
    object Title : ProfileModel()
    data class Row(
        val title: String,
        val caption: String,
        @DrawableRes val icon: Int,
        val action: () -> Unit
    ) : ProfileModel()

    object Subtitle : ProfileModel()
    object Logout : ProfileModel()
    object Error : ProfileModel()
}
