package com.hedvig.app

import androidx.lifecycle.MutableLiveData
import com.hedvig.app.feature.keygear.ui.tab.File
import com.hedvig.app.feature.keygear.ui.tab.KeyGearData
import com.hedvig.app.feature.keygear.ui.tab.KeyGearItem
import com.hedvig.app.feature.keygear.ui.tab.KeyGearItemCategory
import com.hedvig.app.feature.keygear.ui.tab.KeyGearViewModel

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
