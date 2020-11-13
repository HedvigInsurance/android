package com.hedvig.app.mocks

import android.os.Handler
import android.os.Looper.getMainLooper
import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.graphql.CreateKeyGearItemMutation
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.app.feature.keygear.ui.createitem.CreateKeyGearItemViewModel

class MockCreateKeyGearItemViewModel : CreateKeyGearItemViewModel() {
    override val createResult = MutableLiveData<CreateKeyGearItemMutation.Data>()

    override fun createItem() {
        Handler(getMainLooper()).postDelayed({
            when (activeCategory) {
                KeyGearItemCategory.PHONE -> {
                    createResult.postValue(
                        CreateKeyGearItemMutation.Data(
                            CreateKeyGearItemMutation.CreateKeyGearItem(
                                "KeyGearItem",
                                CreateKeyGearItemMutation.CreateKeyGearItem.Fragments(
                                    MockKeyGearItemDetailViewModel.items["123"]!!
                                )
                            )
                        )
                    )
                }
                KeyGearItemCategory.COMPUTER -> {
                    createResult.postValue(
                        CreateKeyGearItemMutation.Data(
                            CreateKeyGearItemMutation.CreateKeyGearItem(
                                "KeyGearItem",
                                CreateKeyGearItemMutation.CreateKeyGearItem.Fragments(
                                    MockKeyGearItemDetailViewModel.items["234"]!!
                                )
                            )
                        )
                    )
                }
                else -> {
                }
            }
        }, 3000)
    }
}
