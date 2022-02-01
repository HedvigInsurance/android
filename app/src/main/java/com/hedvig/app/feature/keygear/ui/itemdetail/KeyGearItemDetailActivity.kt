package com.hedvig.app.feature.keygear.ui.itemdetail

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.widget.NestedScrollView
import androidx.dynamicanimation.animation.SpringAnimation
import com.hedvig.android.owldroid.fragment.KeyGearItemFragment
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityKeyGearItemDetailBinding
import com.hedvig.app.feature.keygear.KeyGearTracker
import com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders.NameBinder
import com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders.PhotosBinder
import com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders.ReceiptBinder
import com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders.ValuationBinder
import com.hedvig.app.util.boundedColorLerp
import com.hedvig.app.util.boundedProgress
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.spring
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class KeyGearItemDetailActivity : BaseActivity(R.layout.activity_key_gear_item_detail) {
    private val model: KeyGearItemDetailViewModel by viewModel()
    private val binding by viewBinding(ActivityKeyGearItemDetailBinding::bind)
    private val tracker: KeyGearTracker by inject()

    private lateinit var photosBinder: PhotosBinder
    private lateinit var valuationBinder: ValuationBinder
    private lateinit var receiptBinder: ReceiptBinder
    private lateinit var nameBinder: NameBinder

    private var isFirstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportPostponeEnterTransition()

        binding.apply {
            window.compatSetDecorFitsSystemWindows(false)
            scrollViewContent.applyNavigationBarInsets()

            initializeToolbar()

            photosBinder = PhotosBinder(
                photosSection,
                intent.getStringExtra(FIRST_PHOTO_URL),
                intent.getSerializableExtra(CATEGORY) as KeyGearItemCategory
            ) { supportStartPostponedEnterTransition() }
            valuationBinder = ValuationBinder(valuationSection, tracker)
            nameBinder = NameBinder(nameSection, model, tracker)
            receiptBinder =
                ReceiptBinder(receiptSection, supportFragmentManager, tracker)
        }

        model.data.observe(this) { data ->
            data?.let { bind(it) }
        }

        model.isDeleted.observe(this) { isDeleted ->
            isDeleted?.let { isd ->
                if (isd) {
                    onBackPressed()
                }
            }
        }
        intent.getStringExtra(ID)?.let { id ->
            model.loadItem(id)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.key_gear_item_detail_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.deleteItem -> {
            model.deleteItem()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun initializeToolbar() {
        binding.apply {
            toolbar.applyStatusBarInsets()
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            val backDrawable = compatDrawable(R.drawable.ic_back)
            backDrawable?.setTint(compatColor(R.color.white))
            toolbar.navigationIcon = backDrawable
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
                        compatColor(R.color.translucent_tool_bar),
                        percentage
                    )
                )
            }
        }
    }

    private fun bind(data: KeyGearItemQuery.KeyGearItem) {
        photosBinder.bind(data)
        valuationBinder.bind(data)
        nameBinder.bind(data)
        receiptBinder.bind(data)

        if (isFirstLoad) {
            revealWithAnimation()
            isFirstLoad = false
        }
    }

    private fun revealWithAnimation() {
        binding.apply {
            postPhotosSections.show()
            val initialTranslation = postPhotosSections.translationY

            postPhotosSections
                .spring(SpringAnimation.TRANSLATION_Y)
                .addUpdateListener { _, value, _ ->
                    val progress = boundedProgress(initialTranslation, 0f, value)
                    postPhotosSections.alpha = progress
                }
                .animateToFinalPosition(0f)
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
                item.photos.getOrNull(0)?.file?.preSignedUrl?.let {
                    putExtra(
                        FIRST_PHOTO_URL,
                        it
                    )
                }
                putExtra(CATEGORY, item.category)
                putExtra(ID, item.id)
            }
    }
}
