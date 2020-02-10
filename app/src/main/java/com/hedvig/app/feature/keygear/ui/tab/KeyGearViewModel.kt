package com.hedvig.app.feature.keygear.ui.tab

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery

abstract class KeyGearViewModel : ViewModel() {
    abstract val data: LiveData<KeyGearItemsQuery.Data>
}

class KeyGearViewModelImpl : KeyGearViewModel() {
    override val data = MutableLiveData<KeyGearItemsQuery.Data>()
}
