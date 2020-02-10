package com.hedvig.app.feature.keygear.ui.createitem

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import timber.log.Timber

class CreateKeyGearViewModel : ViewModel() {
    val photos: MutableLiveData<List<Photo>> = MutableLiveData()
    val categories: MutableLiveData<List<Category>> = MutableLiveData()
    val dirty: MutableLiveData<Boolean> = MutableLiveData()

    init {
        categories.value = listOf(
            Category(KeyGearItemCategory.COMPUTER),
            Category(KeyGearItemCategory.PHONE),
            Category(KeyGearItemCategory.TV),
            Category(KeyGearItemCategory.JEWELRY),
            Category(KeyGearItemCategory.SOUND_SYSTEM)
        )
    }

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
        dirty.value = true
    }

    fun deletePhoto(photo: Photo) {
        if (photos.value == null) {
            Timber.e("Illegal state: Attempted to delete photos, but no photos exist")
            return
        }

        photos.value = photos.value?.filter { p -> p.uri != photo.uri }
        recalculateDirty()
    }

    fun setActiveCategory(category: Category) {
        if (categories.value == null) {
            Timber.e("Illegal state: Attempted to set category, but no categories exist")
        }

        categories.value = categories.value?.map { c ->
            Category(
                c.category,
                selected = category.category == c.category
            )
        }

        dirty.value = true
    }

    private fun recalculateDirty() {
        if (photos.value?.isEmpty() == true && categories.value?.any { c -> c.selected } == false) {
            dirty.value = false
        }
    }
}
