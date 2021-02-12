package com.hedvig.app.feature.embark.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.core.view.updatePaddingRelative
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityMoreOptionsBinding
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.feature.onboarding.MoreOptionsViewModel
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class MoreOptionsActivity : BaseActivity(R.layout.activity_more_options) {
    private val binding by viewBinding(ActivityMoreOptionsBinding::bind)
    private val viewModel: MoreOptionsViewModel by viewModel()
    private val marketProvider: MarketProvider by inject()

    private val showRestart: Boolean by lazy {
        intent.getBooleanExtra(EXTRA_SHOW_RESTART, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            root.setEdgeToEdgeSystemUiFlags(true)
            toolbar.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePaddingRelative(top = initialState.paddings.top + insets.systemWindowInsetTop)
            }

            if (showRestart) {
                restart.doOnApplyWindowInsets { view, insets, initialState ->
                    view.updateMargin(bottom = initialState.margins.bottom + insets.systemWindowInsetBottom)
                }
            } else {
                logIn.doOnApplyWindowInsets { view, insets, initialState ->
                    view.updateMargin(bottom = initialState.margins.bottom + insets.systemWindowInsetBottom)
                }
            }

            setSupportActionBar(toolbar)
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }

            recycler.adapter = MoreOptionsAdapter(viewModel)

            logIn.setHapticClickListener {
                showLogin()
            }

            restart.isVisible = showRestart
            restart.setHapticClickListener {
                restartOffer()
            }

            viewModel.data.observe(this@MoreOptionsActivity) { result ->
                if (result.isFailure) {
                    (recycler.adapter as MoreOptionsAdapter).submitList(
                        listOf(
                            MoreOptionsModel.Header,
                            MoreOptionsModel.UserId.Error,
                            MoreOptionsModel.Version,
                            MoreOptionsModel.Settings,
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
                        MoreOptionsModel.Settings,
                        MoreOptionsModel.Copyright
                    )
                )
            }
        }
    }

    private fun showLogin() {
        marketProvider.market?.openAuth(this, supportFragmentManager)
    }

    private fun restartOffer() {
        setResult(RESULT_RESTART)
        finish()
    }

    companion object {
        const val RESULT_RESTART = 10
        private const val EXTRA_SHOW_RESTART = "show_restart"

        fun newInstance(context: Context, showRestart: Boolean = false) = Intent(context, MoreOptionsActivity::class.java).apply {
            putExtra(EXTRA_SHOW_RESTART, showRestart)
        }
    }
}
