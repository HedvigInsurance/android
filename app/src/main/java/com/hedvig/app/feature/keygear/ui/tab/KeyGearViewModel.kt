package com.hedvig.app.feature.keygear.ui.tab

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class KeyGearData(
    val items: List<KeyGearItem>
)

data class KeyGearItem(
    val photo: File,
    val category: KeyGearItemCategory
)

data class File(
    val signedUrl: String
)

enum class KeyGearItemCategory {
    COMPUTER,
    PHONE,
    TV,
    JEWELRY,
    SOUND_SYSTEM;
}

abstract class KeyGearViewModel : ViewModel() {
    abstract val data: LiveData<KeyGearData>
}

class KeyGearViewModelImpl : KeyGearViewModel() {
    override val data = MutableLiveData<KeyGearData>()
}
