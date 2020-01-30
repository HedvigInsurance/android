package com.hedvig.app.feature.keygear.ui.createitem

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import timber.log.Timber

class CreateKeyGearViewModel : ViewModel() {
    val photos: MutableLiveData<List<Photo>> = MutableLiveData()

    fun addPhotoUri(uri: Uri) {
        if (photos.value == null) {
            photos.postValue(
                listOf(
                    Photo(
                        uri
                    )
                )
            )
        }
        photos.value = photos.value?.toMutableList()?.apply {
            add(Photo(uri))
        }
    }

    fun deletePhoto(photo: Photo) {
        if (photos.value == null) {
            Timber.e("Illegal state: Attempted to delete photos, but no photos exist")
        }

        photos.value = photos.value?.filter { p -> p.uri != photo.uri }
    }
}
