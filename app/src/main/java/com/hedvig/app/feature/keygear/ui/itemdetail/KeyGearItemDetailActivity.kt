package com.hedvig.app.feature.keygear.ui.itemdetail

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import com.hedvig.android.owldroid.fragment.KeyGearItemFragment
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders.PhotosBinder
import com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders.ReceiptBinder
import com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders.ValuationBinder
import com.hedvig.app.util.boundedColorLerp
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.useEdgeToEdge
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import kotlinx.android.synthetic.main.activity_key_gear_item_detail.*
import kotlinx.android.synthetic.main.key_gear_item_detail_photos_section.view.*
import org.koin.android.viewmodel.ext.android.viewModel

class KeyGearItemDetailActivity : BaseActivity(R.layout.activity_key_gear_item_detail) {

    private val model: KeyGearItemDetailViewModel by viewModel()

    private lateinit var photosBinder: PhotosBinder
    private lateinit var valuationBinder: ValuationBinder
    private lateinit var receiptBinder: ReceiptBinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportPostponeEnterTransition()

        root.useEdgeToEdge()
        initializeToolbar()

        photosBinder = PhotosBinder(
            photosSection as FrameLayout,
            intent.getStringExtra(FIRST_PHOTO_URL),
            intent.getSerializableExtra(CATEGORY) as? KeyGearItemCategory
        ) { supportStartPostponedEnterTransition() }
        valuationBinder = ValuationBinder(valuationSection as LinearLayout)
        receiptBinder = ReceiptBinder(receiptSection as LinearLayout, supportFragmentManager)

        model.data.observe(this) { data ->
            data?.let { bind(it) }
        }
        intent.getStringExtra(ID)?.let { id ->
            model.loadItem(id)
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
                scrollY - (photosSection.photos.height - (toolbar.height * 2.0f))
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
        photosBinder.bind(data)
        valuationBinder.bind(data)
        receiptBinder.bind(data)
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
