package com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders

import android.widget.FrameLayout
import androidx.core.view.doOnNextLayout
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.app.feature.keygear.ui.itemdetail.PhotosAdapter
import kotlinx.android.synthetic.main.key_gear_item_detail_photos_section.view.*

class PhotosBinder(
    private val root: FrameLayout,
    firstUrl: String?,
    firstCategory: KeyGearItemCategory?,
    startPostponedTransition: () -> Unit
) {
    init {
        root.photos.adapter = PhotosAdapter(firstUrl, firstCategory)
        root.pagerIndicator.pager = root.photos
        root.photos.doOnNextLayout {
            startPostponedTransition()
        }
    }

    fun bind(data: KeyGearItemQuery.KeyGearItem) {
        (root.photos.adapter as? PhotosAdapter)?.photoUrls =
            data.fragments.keyGearItemFragment.photos.map { it.file.preSignedUrl }
    }
}
