package com.hedvig.app.feature.keygear.ui.createitem

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.hedvig.android.owldroid.graphql.type.KeyGearItemCategory
import com.hedvig.app.R

data class Category(
  val category: KeyGearItemCategory,
  val selected: Boolean = false,
)

@get:StringRes
val KeyGearItemCategory.label: Int
  get() =
    when (this) {
      KeyGearItemCategory.COMPUTER -> hedvig.resources.R.string.ITEM_TYPE_COMPUTER
      KeyGearItemCategory.PHONE -> hedvig.resources.R.string.ITEM_TYPE_PHONE
      KeyGearItemCategory.TV -> hedvig.resources.R.string.ITEM_TYPE_TV
      KeyGearItemCategory.JEWELRY -> hedvig.resources.R.string.ITEM_TYPE_JEWELRY
      KeyGearItemCategory.WATCH -> hedvig.resources.R.string.ITEM_TYPE_WATCH
      KeyGearItemCategory.BIKE -> hedvig.resources.R.string.ITEM_TYPE_BIKE
      KeyGearItemCategory.SMART_WATCH -> hedvig.resources.R.string.ITEM_TYPE_SMART_WATCH
      KeyGearItemCategory.TABLET -> hedvig.resources.R.string.ITEM_TYPE_TABLET
      else -> 0 // Null string resource - causes a crash, use with caution
    }

@get:DrawableRes
val KeyGearItemCategory?.illustration: Int
  get() =
    when (this) {
      KeyGearItemCategory.COMPUTER -> R.drawable.illustration_computer
      KeyGearItemCategory.PHONE -> R.drawable.illustration_phone
      KeyGearItemCategory.TV -> R.drawable.illustration_tv
      KeyGearItemCategory.JEWELRY -> R.drawable.illustration_jewelry
      KeyGearItemCategory.WATCH -> R.drawable.illustration_clock
      KeyGearItemCategory.BIKE -> R.drawable.illustration_bicycle
      KeyGearItemCategory.SMART_WATCH -> R.drawable.illustration_clock
      KeyGearItemCategory.TABLET -> R.drawable.illustration_tablet
      else -> 0 // Null drawable resources - causes a crash, use with caution
    }
