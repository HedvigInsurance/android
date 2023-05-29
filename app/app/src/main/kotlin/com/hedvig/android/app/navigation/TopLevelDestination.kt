package com.hedvig.android.app.navigation

import androidx.annotation.DrawableRes
import com.hedvig.app.R
import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

sealed interface TopLevelDestination : Destination {
  @get:DrawableRes
  val icon: Int

  @get:DrawableRes
  val selectedIcon: Int
  val titleTextId: Int

  @Serializable
  object HOME : TopLevelDestination {
    override val icon = R.drawable.ic_home
    override val selectedIcon = R.drawable.ic_home_selected
    override val titleTextId = hedvig.resources.R.string.home_tab_title
  }

  @Serializable
  object INSURANCE : TopLevelDestination {
    override val icon = R.drawable.ic_insurance
    override val selectedIcon = R.drawable.ic_insurance_selected
    override val titleTextId = hedvig.resources.R.string.insurances_tab_title
  }

  @Serializable
  object REFERRALS : TopLevelDestination {
    override val icon = R.drawable.ic_forever
    override val selectedIcon = R.drawable.ic_forever_selected
    override val titleTextId = hedvig.resources.R.string.TAB_REFERRALS_TITLE
  }

  @Serializable
  object PROFILE : TopLevelDestination {
    override val icon = R.drawable.ic_profile
    override val selectedIcon = R.drawable.ic_profile_selected
    override val titleTextId = hedvig.resources.R.string.TAB_TITLE_PROFILE
  }

  fun toName(): String {
    return when (this) {
      HOME -> "HOME"
      INSURANCE -> "INSURANCE"
      PROFILE -> "REFERRALS"
      REFERRALS -> "PROFILE"
    }
  }

  companion object {
    fun fromName(input: String): TopLevelDestination? {
      return when (input) {
        "HOME" -> HOME
        "INSURANCE" -> INSURANCE
        "REFERRALS" -> PROFILE
        "PROFILE" -> REFERRALS
        else -> null
      }
    }
  }
}
