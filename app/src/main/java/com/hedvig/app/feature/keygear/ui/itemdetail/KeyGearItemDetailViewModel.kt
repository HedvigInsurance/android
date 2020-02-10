package com.hedvig.app.feature.keygear.ui.itemdetail

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.app.util.LiveEvent

abstract class KeyGearItemDetailViewModel : ViewModel() {
    abstract val data: LiveData<KeyGearItemQuery.KeyGearItem>

    abstract val isUploading: LiveEvent<Boolean>

    abstract fun loadItem(id: String)
    abstract fun uploadReceipt(uri: Uri)
}

class KeyGearItemDetailViewModelImpl : KeyGearItemDetailViewModel() {
    override val data = MutableLiveData<KeyGearItemQuery.KeyGearItem>()
    override val isUploading = LiveEvent<Boolean>()

    override fun loadItem(id: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun uploadReceipt(uri: Uri) {
        isUploading.value = true
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
