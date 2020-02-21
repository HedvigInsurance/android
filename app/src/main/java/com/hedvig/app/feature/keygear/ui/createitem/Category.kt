package com.hedvig.app.feature.keygear.ui.createitem

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.app.R

data class Category(
    val category: KeyGearItemCategory,
    val selected: Boolean = false
)

@get:StringRes
val KeyGearItemCategory.label: Int
    get() =
        when (this) {
            KeyGearItemCategory.COMPUTER -> R.string.ITEM_TYPE_COMPUTER
            KeyGearItemCategory.PHONE -> R.string.ITEM_TYPE_PHONE
            KeyGearItemCategory.TV -> R.string.ITEM_TYPE_TV
            KeyGearItemCategory.JEWELRY -> R.string.ITEM_TYPE_JEWELRY
            KeyGearItemCategory.WATCH -> R.string.ITEM_TYPE_WATCH
            KeyGearItemCategory.SMART_WATCH -> R.string.ITEM_TYPE_WATCH //TODO get correct translation
            else -> 0 // Null string resource - does not cause a crash, simply does nothing
        }

@get:DrawableRes
val KeyGearItemCategory?.illustration: Int
    get() =
        when (this) {
            KeyGearItemCategory.COMPUTER -> R.drawable.illustration_computer
            KeyGearItemCategory.PHONE -> R.drawable.illustration_phone
            KeyGearItemCategory.TV -> R.drawable.illustration_tv
            KeyGearItemCategory.JEWELRY -> R.drawable.illustration_jewelry
            KeyGearItemCategory.WATCH -> R.drawable.illustration_jewelry //TODO get illustration
            KeyGearItemCategory.SMART_WATCH -> R.drawable.illustration_jewelry //TODO get illustration
            else -> 0
        }
