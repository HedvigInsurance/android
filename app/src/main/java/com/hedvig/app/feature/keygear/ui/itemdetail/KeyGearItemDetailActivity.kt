package com.hedvig.app.feature.keygear.ui.itemdetail

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.core.view.doOnNextLayout
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.boundedColorLerp
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.useEdgeToEdge
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import kotlinx.android.synthetic.main.activity_key_gear_item_detail.*
import org.koin.android.viewmodel.ext.android.viewModel

class KeyGearItemDetailActivity : BaseActivity(R.layout.activity_key_gear_item_detail) {

    private val model: KeyGearItemDetailViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportPostponeEnterTransition()

        root.useEdgeToEdge()
        initializeToolbar()
        initializePhotos(
            intent.getStringExtra(FIRST_PHOTO_URL),
            intent.getSerializableExtra(
                FIRST_PHOTO_CATEGORY
            ) as? KeyGearItemCategory
        )

        model.data.observe(this) { data ->
            data?.let { bind(it) }
        }
    }

    private fun initializePhotos(photoUrl: String?, category: KeyGearItemCategory?) {
        photos.adapter = PhotosAdapter(photoUrl, category)
        pagerIndicator.pager = photos
        photos.doOnNextLayout {
            supportStartPostponedEnterTransition()
        }
    }

    private fun initializeToolbar() {
        toolbar.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = compatDrawable(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        scrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            val positionInSpan =
                scrollY - (photos.height - (toolbar.height * 2.0f))
            val percentage = positionInSpan / toolbar.height

            // Avoid some unnecessary background color updates
            if (percentage < -1 || percentage > 2) {
                return@setOnScrollChangeListener
            }

            toolbar.setBackgroundColor(
                boundedColorLerp(
                    Color.TRANSPARENT,
                    compatColor(R.color.background_elevation_1),
                    percentage
                )
            )
        }
    }

    private fun bind(data: KeyGearItemsQuery.KeyGearItemsSimple) {
        (photos.adapter as? PhotosAdapter)?.photoUrls = data.photos.map { it.file.preSignedUrl }
    }

    companion object {
        private const val FIRST_PHOTO_URL = "FIRST_PHOTO_URL"
        private const val FIRST_PHOTO_CATEGORY = "FIRST_PHOTO_CATEGORY"

        fun newInstance(
            context: Context,
            photoUrl: String? = null,
            category: KeyGearItemCategory? = null
        ) =
            Intent(context, KeyGearItemDetailActivity::class.java).apply {
                photoUrl?.let { putExtra(FIRST_PHOTO_URL, it) }
                category?.let { putExtra(FIRST_PHOTO_CATEGORY, it) }
            }
    }
}
