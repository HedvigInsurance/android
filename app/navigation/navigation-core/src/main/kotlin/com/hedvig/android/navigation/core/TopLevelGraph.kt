package com.hedvig.android.navigation.core

import androidx.annotation.DrawableRes
import com.kiwi.navigationcompose.typed.Destination
import hedvig.resources.R
import kotlinx.serialization.Serializable

sealed interface TopLevelGraph : Destination {
  @get:DrawableRes
  val icon: Int

  @get:DrawableRes
  val selectedIcon: Int
  val titleTextId: Int

  @Serializable
  object HOME : TopLevelGraph {
    override val icon = R.drawable.ic_navigation_home
    override val selectedIcon = R.drawable.ic_navigation_home_selected
    override val titleTextId = R.string.home_tab_title
  }

  @Serializable
  object INSURANCE : TopLevelGraph {
    override val icon = R.drawable.ic_navigation_insurance
    override val selectedIcon = R.drawable.ic_navigation_insurance_selected
    override val titleTextId = R.string.insurances_tab_title
  }

  @Serializable
  object FOREVER : TopLevelGraph {
    override val icon = R.drawable.ic_navigation_forever
    override val selectedIcon = R.drawable.ic_navigation_forever_selected
    override val titleTextId = R.string.TAB_REFERRALS_TITLE
  }

  @Serializable
  object PROFILE : TopLevelGraph {
    override val icon = R.drawable.ic_navigation_profile
    override val selectedIcon = R.drawable.ic_navigation_profile_selected
    override val titleTextId = R.string.TAB_TITLE_PROFILE
  }

  fun toName(): String {
    return when (this) {
      HOME -> "HOME"
      INSURANCE -> "INSURANCE"
      FOREVER -> "REFERRALS"
      PROFILE -> "PROFILE"
    }
  }

  companion object {
    fun fromName(input: String): TopLevelGraph? {
      return when (input) {
        "HOME" -> HOME
        "INSURANCE" -> INSURANCE
        "REFERRALS" -> FOREVER
        "PROFILE" -> PROFILE
        else -> null
      }
    }
  }
}
