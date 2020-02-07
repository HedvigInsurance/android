package com.hedvig.app

import android.os.Handler
import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailViewModel

class MockKeyGearItemDetailViewModel : KeyGearItemDetailViewModel() {
    override val data = MutableLiveData<KeyGearItemsQuery.KeyGearItemsSimple>()

    init {
        Handler().postDelayed({
            data.postValue(
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
                )
            )
        }, 1000)
    }
}
