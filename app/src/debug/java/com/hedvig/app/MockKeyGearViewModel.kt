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
                        listOf(
                            KeyGearItemsQuery.Photo(
                                "KeyGearItemPhoto",
                                KeyGearItemsQuery.File(
                                    "S3File",
                                    "https://images.unsplash.com/photo-1505156868547-9b49f4df4e04"
                                )
                            )
                        ),
                        KeyGearItemCategory.PHONE
                    ),
                    KeyGearItemsQuery.KeyGearItemsSimple(
                        "KeyGearItem",
                        "123",
                        listOf(
                            KeyGearItemsQuery.Photo(
                                "KeyGearItemPhoto",
                                KeyGearItemsQuery.File(
                                    "S3File",
                                    "https://images.unsplash.com/photo-1522199755839-a2bacb67c546?"
                                )
                            )
                        ),
                        KeyGearItemCategory.COMPUTER
                    )
                )
            )
        )
    }
}
