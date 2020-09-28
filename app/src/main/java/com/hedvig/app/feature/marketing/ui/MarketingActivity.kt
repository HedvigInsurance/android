package com.hedvig.app.feature.marketing.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityMarketingBinding
import com.hedvig.app.feature.marketpicker.MarketPickerFragment
import com.hedvig.app.feature.marketpicker.MarketSelectedFragment
import com.hedvig.app.util.BlurHashDecoder
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import org.koin.android.viewmodel.ext.android.viewModel

class MarketingActivity : BaseActivity(R.layout.activity_marketing) {
    private val model: MarketingViewModel by viewModel()

    private val binding by viewBinding(ActivityMarketingBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            root.setEdgeToEdgeSystemUiFlags(true)

            supportFragmentManager.beginTransaction().add(R.id.container, MarketPickerFragment())
                .commit()

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
