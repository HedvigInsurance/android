package com.hedvig.app.feature.embark.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.updatePaddingRelative
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityMoreOptionsBinding
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.onboarding.MoreOptionsModel
import com.hedvig.app.feature.onboarding.MemberIdViewModel
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import org.koin.androidx.viewmodel.ext.android.viewModel

class MoreOptionsActivity : BaseActivity(R.layout.activity_more_options) {
    private val binding by viewBinding(ActivityMoreOptionsBinding::bind)
    private val viewModel: MemberIdViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            root.setEdgeToEdgeSystemUiFlags(true)
            toolbar.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePaddingRelative(top = initialState.paddings.top + insets.systemWindowInsetTop)
            }

            setSupportActionBar(toolbar)
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }

            recycler.adapter = MoreOptionsAdapter(model)

            model.data.observe(this@MoreOptionsActivity) { result ->
                if (result.isFailure) {
                    (recycler.adapter as MoreOptionsAdapter).submitList(
                        listOf(
                            MoreOptionsModel.Header,
                            MoreOptionsModel.UserId.Error,
                            MoreOptionsModel.Version,
                            MoreOptionsModel.Copyright
                        )
                    )
                    return@observe
                }
                (recycler.adapter as MoreOptionsAdapter).submitList(
                    listOf(
                        MoreOptionsModel.Header,
                        result.getOrNull()?.member?.id?.let { MoreOptionsModel.UserId.Success(it) },
                        MoreOptionsModel.Version,
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
