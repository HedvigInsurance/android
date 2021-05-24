package com.hedvig.app.feature.marketing.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.google.android.material.transition.MaterialContainerTransform
import com.hedvig.android.owldroid.type.UserInterfaceStyle
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityMarketingBinding
import com.hedvig.app.feature.marketpicker.CurrentFragment.MARKETING
import com.hedvig.app.feature.marketpicker.CurrentFragment.MARKET_PICKER
import com.hedvig.app.feature.marketpicker.MarketPickerFragment
import com.hedvig.app.feature.marketpicker.MarketSelectedFragment
import com.hedvig.app.util.BlurHashDecoder
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import org.koin.androidx.viewmodel.ext.android.viewModel

class MarketingActivity : BaseActivity(R.layout.activity_marketing) {
    private val model: MarketingViewModel by viewModel()
    private val binding by viewBinding(ActivityMarketingBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model.navigationState.observe(this) { navigationState ->
            when (navigationState.destination) {
                MARKET_PICKER -> replaceFragment(MarketPickerFragment(), navigationState, MARKET_PICKER_FRAGMENT_TAG)
                MARKETING -> replaceFragment(MarketSelectedFragment(), navigationState, MARKET_FRAGMENT_TAG)
            }
        }

        binding.apply {
            root.setEdgeToEdgeSystemUiFlags(true)

            model
                .marketingBackground
                .observe(this@MarketingActivity) { image ->
                    val placeholder = BlurHashDecoder.decode(image.blurhash, 32, 32)
                    Glide
                        .with(this@MarketingActivity)
                        .load(image.image?.url)
                        .transition(withCrossFade())
                        .placeholder(BitmapDrawable(resources, placeholder))
                        .into(backgroundImage)

                    when (image.userInterfaceStyle) {
                        UserInterfaceStyle.LIGHT -> {
                            val view = window.decorView
                            view.systemUiVisibility =
                                view.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                        }
                        UserInterfaceStyle.DARK, UserInterfaceStyle.UNKNOWN__ -> {
                            val view = window.decorView
                            view.systemUiVisibility =
                                view.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        }
                    }
                }
        }
    }

    private fun replaceFragment(fragment: Fragment, navigationState: NavigationState, tag: String) {
        supportFragmentManager.commit {
            replace(
                R.id.container,
                fragment.also { it.sharedElementEnterTransition = MaterialContainerTransform() },
                tag
            )

            if (navigationState.addToBackStack) {
                addToBackStack(null)
            }

            setReorderingAllowed(navigationState.reorderingAllowed)
            navigationState.sharedElements.map {
                addSharedElement(it.first, it.second)
            }
        }
    }

    companion object {
        const val SHARED_ELEMENT_NAME = "marketButton"

        private const val MARKET_FRAGMENT_TAG = "market"
        private const val MARKET_PICKER_FRAGMENT_TAG = "picker"

        fun newInstance(context: Context, withoutHistory: Boolean = false) =
            Intent(context, MarketingActivity::class.java).apply {
                if (withoutHistory) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
            }
    }
}
