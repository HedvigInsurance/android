package com.hedvig.app.feature.keygear.ui.createitem

import com.hedvig.android.owldroid.type.KeyGearItemCategory

data class Category(
    val category: KeyGearItemCategory,
    val selected: Boolean = false
)

// TODO: Add translations when available
val KeyGearItemCategory.label: String
    get() =
        when (this) {
            KeyGearItemCategory.COMPUTER -> "Dator"
            KeyGearItemCategory.PHONE -> "Mobiltelefon"
            KeyGearItemCategory.TV -> "TV"
            KeyGearItemCategory.JEWELRY -> "Smycke"
            KeyGearItemCategory.SOUND_SYSTEM -> "Ljudanläggning"
            else -> ""
        }
