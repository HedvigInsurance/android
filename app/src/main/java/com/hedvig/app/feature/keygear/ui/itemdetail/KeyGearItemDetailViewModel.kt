package com.hedvig.app.feature.keygear.ui.itemdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery

abstract class KeyGearItemDetailViewModel : ViewModel() {
    abstract val data: LiveData<KeyGearItemQuery.KeyGearItem>
   
    abstract fun loadItem(id: String)
}

class KeyGearItemDetailViewModelImpl : KeyGearItemDetailViewModel() {
    override val data = MutableLiveData<KeyGearItemQuery.KeyGearItem>()

    override fun loadItem(id: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
