package com.hedvig.app.feature.keygear.ui.itemdetail

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.dynamicanimation.animation.SpringAnimation
import com.hedvig.android.owldroid.fragment.KeyGearItemFragment
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.ui.itemdetail.binders.CoverageBinder
import com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders.PhotosBinder
import com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders.ReceiptBinder
import com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders.ValuationBinder
import com.hedvig.app.util.boundedColorLerp
import com.hedvig.app.util.boundedProgress
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.dismissKeyboard
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.useEdgeToEdge
import com.hedvig.app.util.spring
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import kotlinx.android.synthetic.main.activity_key_gear_item_detail.*
import kotlinx.android.synthetic.main.key_gear_item_detail_photos_section.view.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class KeyGearItemDetailActivity : BaseActivity(R.layout.activity_key_gear_item_detail) {

    private val model: KeyGearItemDetailViewModel by viewModel()

    private lateinit var photosBinder: PhotosBinder
    private lateinit var valuationBinder: ValuationBinder
    private lateinit var coverageBinder: CoverageBinder
    private lateinit var receiptBinder: ReceiptBinder

    private var isFirstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportPostponeEnterTransition()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)


        root.useEdgeToEdge()
        initializeToolbar()

        photosBinder = PhotosBinder(
            photosSection as FrameLayout,
            intent.getStringExtra(FIRST_PHOTO_URL),
            intent.getSerializableExtra(CATEGORY) as KeyGearItemCategory
        ) { supportStartPostponedEnterTransition() }
        valuationBinder = ValuationBinder(valuationSection as LinearLayout)
        coverageBinder = CoverageBinder(coverageSection as LinearLayout)
        receiptBinder = ReceiptBinder(receiptSection as LinearLayout, supportFragmentManager)

        scrollViewContent.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)

        }


        scrollView.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(
                bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom
            )
        }

        addName.setHapticClickListener {
            focusEditName()
        }

        nameEditText.setDoneListener {
            hideEditName()
            //TODO Upload new name
            Timber.d("Done")
        }

        saveName.setHapticClickListener {
            //TODO Upload new name
            hideEditName()


            model.data.observe(this) { data ->
                data?.let { bind(it) }
            }
            intent.getStringExtra(ID)?.let { id ->
                model.loadItem(id)
            }
        }
    }

    private fun focusEditName() {
        name.animate().alpha(0.0f).withEndAction {
            name.remove()
        }.duration = ANIMATE_DURATION

        addName.animate().alpha(0.0f).withEndAction {
            addName.remove()
        }.duration = ANIMATE_DURATION


        nameEditText.apply {
            alpha = 0.0f
            show()
            nameEditText.animate().alpha(1.0f).withEndAction {
                nameEditText.openKeyBoard()
            }.duration = ANIMATE_DURATION
        }

        saveName.apply {
            alpha = 0.0f
            show()
            saveName
                .animate()
                .alpha(1.0f)
                .duration = ANIMATE_DURATION
        }
    }

    private fun hideEditName() {
        saveName.animate()
            .alpha(0.0f)
            .withEndAction {
                saveName.remove()
            }.duration = ANIMATE_DURATION

        nameEditText.apply {
            animate()
                .alpha(0f).withEndAction {
                    nameEditText.dismissKeyboard()
                    nameEditText.remove()
                }.duration = ANIMATE_DURATION
        }

        name.apply {
            alpha = 0.0f
            show()
            animate()
                .alpha(1.0f)
                .duration = ANIMATE_DURATION
        }

        addName.apply {
            alpha = 0.0f
            show()
            animate()
                .alpha(1.0f)
                .duration = ANIMATE_DURATION
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
        val newPhotos: MutableList<String?> =
            data.fragments.keyGearItemFragment.photos.map { it.file.preSignedUrl }
                .toMutableList()
        if (newPhotos.isEmpty()) {
            newPhotos.add(intent.getStringExtra(FIRST_PHOTO_URL))
            photosBinder.bind(data)
            valuationBinder.bind(data)
            coverageBinder.bind(data)
            receiptBinder.bind(data)

            if (isFirstLoad) {
                revealWithAnimation()
                isFirstLoad = false
            }
        }
    }

    private fun revealWithAnimation() {
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

    companion object {
        private const val FIRST_PHOTO_URL = "FIRST_PHOTO_URL"
        private const val CATEGORY = "CATEGORY"
        private const val ID = "ID"
        private const val ANIMATE_DURATION = 60L

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
