package com.hedvig.app.feature.marketing.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.google.android.material.transition.MaterialContainerTransform
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityMarketingBinding
import com.hedvig.app.feature.marketpicker.CurrentFragment.MARKETING
import com.hedvig.app.feature.marketpicker.CurrentFragment.MARKET_PICKER
import com.hedvig.app.feature.marketpicker.MarketPickerFragment
import com.hedvig.app.feature.marketpicker.MarketSelectedFragment
import com.hedvig.app.util.BlurHashDecoder
import com.hedvig.app.util.extensions.getMarket
import com.hedvig.app.util.extensions.makeToast
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import org.koin.android.viewmodel.ext.android.viewModel

class MarketingActivity : BaseActivity(R.layout.activity_marketing) {
    private val model: MarketingViewModel by viewModel()
    private val binding by viewBinding(ActivityMarketingBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        model.navigationState.observe(this) { navigationState ->
            when (navigationState.destination) {
                MARKET_PICKER -> {
                    val transaction = supportFragmentManager.beginTransaction()
                    navigationState.sharedElements.forEach { (v, t) ->
                        transaction.addSharedElement(v, t)
                    }
                    transaction.replace(R.id.container, MarketPickerFragment())
                        .commit()
                }
                MARKETING -> {
                    val transaction = supportFragmentManager.beginTransaction()
                    navigationState.sharedElements.forEach { (view, transitionName) ->
                        transaction.addSharedElement(view, transitionName)
                    }
                    transaction.replace(R.id.container, MarketSelectedFragment().also {
                        it.sharedElementEnterTransition = MaterialContainerTransform()
                    }).commit()
                }
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
                }
        }
    }

    companion object {
        fun newInstance(context: Context, withoutHistory: Boolean = false) =
            Intent(context, MarketingActivity::class.java).apply {
                if (withoutHistory) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
            }
    }
}
