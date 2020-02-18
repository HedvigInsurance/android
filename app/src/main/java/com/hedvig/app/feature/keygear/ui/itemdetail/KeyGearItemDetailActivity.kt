package com.hedvig.app.feature.keygear.ui.itemdetail

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.core.view.doOnNextLayout
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import com.hedvig.android.owldroid.fragment.KeyGearItemFragment
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.ui.ReceiptActivity
import com.hedvig.app.util.boundedColorLerp
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.setHapticClickListener
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
            intent.getSerializableExtra(CATEGORY) as KeyGearItemCategory
        )

        model.data.observe(this) { data ->
            data?.let { bind(it) }
        }
        intent.getStringExtra(ID)?.let { id ->
            model.loadItem(id)
        }
    }

    private fun initializePhotos(photoUrl: String?, category: KeyGearItemCategory) {
        var firstPhotoDidLoad = false
        photos.adapter = PhotosAdapter(photoUrl, category) {
            if (!firstPhotoDidLoad && photoUrl != null) {
                firstPhotoDidLoad = true
                supportStartPostponedEnterTransition()
            }
        }
        pagerIndicator.pager = photos
        photos.doOnNextLayout {
            if (photoUrl == null) {
                firstPhotoDidLoad = true
                supportStartPostponedEnterTransition()
            }
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
                    compatColor(R.color.translucent_background),
                    percentage
                )
            )
        }
    }

    private fun bind(data: KeyGearItemQuery.KeyGearItem) {
        val newPhotos: MutableList<String?> = data.fragments.keyGearItemFragment.photos.map { it.file.preSignedUrl }.toMutableList()
        if (newPhotos.isEmpty()) {
            newPhotos.add(intent.getStringExtra(FIRST_PHOTO_URL))
        }
        (photos.adapter as? PhotosAdapter)?.photoUrls = newPhotos

        data.fragments.keyGearItemFragment.receipts.getOrNull(0)?.let { receipt ->
            addOrViewReceipt.text = getString(R.string.KEY_GEAR_ITEM_VIEW_RECEIPT_SHOW)
            addOrViewReceipt.setHapticClickListener {
                startActivity(ReceiptActivity.newInstance(this, receipt.file.preSignedUrl))
            }
        } ?: run {
            addOrViewReceipt.text = getString(R.string.KEY_GEAR_ITEM_VIEW_RECEIPT_CELL_ADD_BUTTON)
            addOrViewReceipt.setHapticClickListener {
                ReceiptFileUploadBottomSheet
                    .newInstance()
                    .show(supportFragmentManager, ReceiptFileUploadBottomSheet.TAG)
            }
        }
    }

    companion object {
        private const val FIRST_PHOTO_URL = "FIRST_PHOTO_URL"
        private const val CATEGORY = "CATEGORY"
        private const val ID = "ID"

        fun newInstance(
            context: Context,
            item: KeyGearItemFragment
        ) =
            Intent(context, KeyGearItemDetailActivity::class.java).apply {
                item.photos.getOrNull(0)?.file?.preSignedUrl?.let { putExtra(FIRST_PHOTO_URL, it) }
                putExtra(CATEGORY, item.category)
                putExtra(ID, item.id)
            }
    }
}
