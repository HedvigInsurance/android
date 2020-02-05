package com.hedvig.app

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.app.feature.keygear.ui.tab.KeyGearViewModel

class MockKeyGearViewModel : KeyGearViewModel() {
    override val data = MutableLiveData<KeyGearItemsQuery.Data>()

    init {
        data.postValue(
            KeyGearItemsQuery.Data(
                listOf(
                    KeyGearItemsQuery.KeyGearItemsSimple(
                        "KeyGearItem",
                        "123",
                        listOf(),
                        KeyGearItemCategory.PHONE
                    ),
                    KeyGearItemsQuery.KeyGearItemsSimple(
                        "KeyGearItem",
                        "123",
                        listOf(),
                        KeyGearItemCategory.PHONE
                    )
                )
            )
        )
    }
}
