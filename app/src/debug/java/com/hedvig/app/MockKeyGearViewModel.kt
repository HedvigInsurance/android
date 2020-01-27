package com.hedvig.app

import androidx.lifecycle.MutableLiveData
import com.hedvig.app.feature.keygear.File
import com.hedvig.app.feature.keygear.KeyGearData
import com.hedvig.app.feature.keygear.KeyGearItem
import com.hedvig.app.feature.keygear.KeyGearItemCategory
import com.hedvig.app.feature.keygear.KeyGearViewModel

class MockKeyGearViewModel : KeyGearViewModel() {
    override val data = MutableLiveData<KeyGearData>()

    init {
        data.postValue(
            KeyGearData(
                listOf(
                    KeyGearItem(
                        File("https://picsum.photos/160/200"),
                        KeyGearItemCategory.PHONE
                    ),
                    KeyGearItem(
                        File("https://picsum.photos/160/200"),
                        KeyGearItemCategory.COMPUTER
                    )
                )
            )
        )
    }
}
