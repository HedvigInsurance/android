package com.hedvig.app

import android.os.Handler
import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.KeyGearItemFragment
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailViewModel

class MockKeyGearItemDetailViewModel : KeyGearItemDetailViewModel() {
    override val data = MutableLiveData<KeyGearItemQuery.KeyGearItem>()

    override fun loadItem(id: String) {
        Handler().postDelayed({
            data.postValue(
                KeyGearItemQuery.KeyGearItem(
                    "KeyGearItem",
                    KeyGearItemQuery.KeyGearItem.Fragments(items[id]!!)
                )
            )
        }, 250)
    }

    companion object {
        val items = hashMapOf(
            "123" to
                KeyGearItemFragment(
                    "KeyGearItem",
                    "123",
                    listOf(
                        KeyGearItemFragment.Photo(
                            "KeyGearItemPhoto",
                            KeyGearItemFragment.File(
                                "S3File",
                                "https://images.unsplash.com/photo-1505156868547-9b49f4df4e04"
                            )
                        )
                    ),
                    KeyGearItemCategory.PHONE
                ),
            "234" to
                KeyGearItemFragment(
                    "KeyGearItem",
                    "234",
                    listOf(
                        KeyGearItemFragment.Photo(
                            "KeyGearItemPhoto",
                            KeyGearItemFragment.File(
                                "S3File",
                                "https://images.unsplash.com/photo-1522199755839-a2bacb67c546"
                            )
                        )
                    ),
                    KeyGearItemCategory.COMPUTER
                )
        )
    }
}
