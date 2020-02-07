package com.hedvig.app.feature.keygear.ui.itemdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery

abstract class KeyGearItemDetailViewModel : ViewModel() {
    // TODO: Replace with the correct query
    abstract val data: LiveData<KeyGearItemsQuery.KeyGearItemsSimple>
}

class KeyGearItemDetailViewModelImpl : KeyGearItemDetailViewModel() {
    override val data = MutableLiveData<KeyGearItemsQuery.KeyGearItemsSimple>()
}
