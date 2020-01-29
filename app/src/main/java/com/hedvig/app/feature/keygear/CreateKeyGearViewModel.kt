package com.hedvig.app.feature.keygear

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreateKeyGearViewModel : ViewModel() {
    fun addPhotoUri(uri: Uri) {
        if (photos.value == null) {
            photos.postValue(listOf(Photo(uri)))
        }
        photos.value = photos.value?.toMutableList()?.apply {
            add(Photo(uri))
        }
    }

    val photos: MutableLiveData<List<Photo>> = MutableLiveData()
}
