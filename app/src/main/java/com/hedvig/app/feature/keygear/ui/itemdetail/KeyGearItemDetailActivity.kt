package com.hedvig.app.feature.keygear.ui.itemdetail

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.core.view.doOnNextLayout
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.boundedColorLerp
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatDrawable
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
        toolbar.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = compatDrawable(R.drawable.ic_back)
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

        photos.adapter = PhotosAdapter(
            listOf(
                Photo("https://images.unsplash.com/photo-1505156868547-9b49f4df4e04"),
                Photo("https://images.unsplash.com/photo-1505156868547-9b49f4df4e04"),
                Photo("https://images.unsplash.com/photo-1505156868547-9b49f4df4e04")
            )
        )
        photos.doOnNextLayout {
            supportStartPostponedEnterTransition()
        }
        pagerIndicator.pager = photos
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, KeyGearItemDetailActivity::class.java)
    }
}
