package com.hedvig.app.feature.onboarding.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.updatePaddingRelative
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityMoreOptionsBinding
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.onboarding.MoreOptionsModel
import com.hedvig.app.feature.onboarding.MoreOptionsViewModel
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import org.koin.android.viewmodel.ext.android.viewModel

class MoreOptionsActivity : BaseActivity(R.layout.activity_more_options) {
    private val binding by viewBinding(ActivityMoreOptionsBinding::bind)
    private val viewModel: MoreOptionsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            root.setEdgeToEdgeSystemUiFlags(true)
            toolbar.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePaddingRelative(top = initialState.paddings.top + insets.systemWindowInsetTop)
            }
            logIn.doOnApplyWindowInsets { view, insets, initialState ->
                view.updateMargin(bottom = initialState.margins.bottom + insets.systemWindowInsetBottom)
            }
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }

            logIn.setHapticClickListener {
                startActivity(MarketingActivity.newInstance(this@MoreOptionsActivity, true))
            }

            recycler.adapter = MoreOptionsAdapter(viewModel)
            viewModel.data.observe(this@MoreOptionsActivity) { result ->
                (recycler.adapter as MoreOptionsAdapter).submitList(
                    listOf(
                        MoreOptionsModel.Header,
                        result.getOrNull()?.member?.id?.let { MoreOptionsModel.Row.UserId.Success(it) }
                            ?: MoreOptionsModel.Row.UserId.Error,
                        MoreOptionsModel.Row.Version,
                        MoreOptionsModel.Row.Settings,
                        MoreOptionsModel.Copyright
                    )
                )
            }
        }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, MoreOptionsActivity::class.java)
    }
}
