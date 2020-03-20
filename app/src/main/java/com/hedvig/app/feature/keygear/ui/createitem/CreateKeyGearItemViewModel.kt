package com.hedvig.app.feature.keygear.ui.createitem

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.CreateKeyGearItemMutation
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.android.owldroid.type.S3FileInput
import com.hedvig.app.feature.keygear.data.KeyGearItemsRepository
import e
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class CreateKeyGearItemViewModel : ViewModel() {
    abstract val createResult: LiveData<CreateKeyGearItemMutation.Data>

    abstract fun createItem()

    val photos = MutableLiveData<List<Photo>>()
    val categories = MutableLiveData<List<Category>>()
    val dirty = MutableLiveData<Boolean>()

    protected val activeCategory: KeyGearItemCategory?
        get() = categories.value?.find { it.selected }?.category

    init {
        categories.value = KeyGearItemCategory
            .values()
            .filter { it != KeyGearItemCategory.UNKNOWN__ }
            .map { Category(it) }
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

class CreateKeyGearItemViewModelImpl(
    private val keyGearItemsRepository: KeyGearItemsRepository
) : CreateKeyGearItemViewModel() {
    override val createResult = MutableLiveData<CreateKeyGearItemMutation.Data>()

    override fun createItem() {
        viewModelScope.launch {
            val category = activeCategory ?: return@launch
            val photos = photos.value?.map { it.uri } ?: listOf()
            val uploads = if (photos.isNotEmpty()) {
                val uploadsResponse = runCatching {
                    keyGearItemsRepository.uploadPhotosForNewKeyGearItemAsync(photos).await()
                }
                if (uploadsResponse.isFailure) {
                    uploadsResponse.exceptionOrNull()?.let { e(it) }
                }
                uploadsResponse.getOrNull()?.let { response ->
                    response.data()?.uploadFiles?.map {
                        S3FileInput(bucket = it.bucket, key = it.key)
                    } ?: return@launch
                }
            } else {
                emptyList()
            }
            val result = runCatching {
                uploads?.let { uploads ->
                    keyGearItemsRepository.createKeyGearItemAsync(
                        category,
                        uploads
                    )
                }
            }
            if (result.isFailure) {
                result.exceptionOrNull()?.let { e(it) }
            }
            result.getOrNull()?.let { createResult.postValue(it.data()) }
        }
    }
}
