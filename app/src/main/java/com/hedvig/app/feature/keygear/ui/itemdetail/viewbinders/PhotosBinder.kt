package com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders

import androidx.core.view.doOnNextLayout
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.app.databinding.KeyGearItemDetailPhotosSectionBinding
import com.hedvig.app.feature.keygear.ui.itemdetail.PhotosAdapter

class PhotosBinder(
    private val root: KeyGearItemDetailPhotosSectionBinding,
    private val firstUrl: String?,
    firstCategory: KeyGearItemCategory,
    startPostponedTransition: () -> Unit
) {
    init {
        var firstPhotoDidLoad = false
        root.photos.adapter = PhotosAdapter(firstUrl, firstCategory) {
            if (!firstPhotoDidLoad && firstUrl != null) {
                firstPhotoDidLoad = true
                startPostponedTransition()
            }
        }
        root.pagerIndicator.pager = root.photos
        root.photos.doOnNextLayout {
            if (firstUrl == null) {
                firstPhotoDidLoad = true
                startPostponedTransition()
            }
        }
    }

    fun bind(data: KeyGearItemQuery.KeyGearItem) {
        val newPhotos: MutableList<String?> =
            data.fragments.keyGearItemFragment.photos.map { it.file.preSignedUrl }.toMutableList()
        if (newPhotos.isEmpty()) {
            newPhotos.add(firstUrl)
        }
        (root.photos.adapter as? PhotosAdapter)?.photoUrls = newPhotos
    }
}
