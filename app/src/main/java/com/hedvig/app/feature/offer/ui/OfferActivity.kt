package com.hedvig.app.feature.offer.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityOfferBinding
import com.hedvig.app.feature.documents.DocumentAdapter
import com.hedvig.app.feature.embark.ui.MoreOptionsActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.offer.OfferSignDialog
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.service.LoginStatusService.Companion.IS_VIEWING_OFFER
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.storeBoolean
import com.hedvig.app.util.extensions.view.hide
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class OfferActivity : BaseActivity(R.layout.activity_offer) {
    private val model: OfferViewModel by viewModel()
    private val binding by viewBinding(ActivityOfferBinding::bind)
    private val tracker: OfferTracker by inject()
    private val marketManager: MarketManager by inject()

    private var scrollInitialPaddingTop = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            offerRoot.setEdgeToEdgeSystemUiFlags(true)
            scrollInitialPaddingTop = offerScroll.paddingTop
            offerToolbar.background.alpha = 0

            offerToolbar.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
                applyInsets(view.height)
            }
            signButton.doOnApplyWindowInsets { view, insets, initialState ->
                view.updateMargin(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
            }

            offerScroll.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                private var scrollY = 0
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    scrollY += dy
                    val percentage = scrollY.toFloat() / offerToolbar.height
                    offerToolbar.background.alpha = (percentage * 255).toInt().coerceAtMost(255)

                    if (percentage > 4 && !signButton.isVisible) {
                        TransitionManager.beginDelayedTransition(offerRoot)
                        signButton.show()
                    } else if (percentage < 4 && signButton.isVisible) {
                        TransitionManager.beginDelayedTransition(offerRoot)
                        signButton.hide()
                    }
                }
            })

            offerToolbar.setNavigationOnClickListener { onBackPressed() }
            offerToolbar.setOnMenuItemClickListener(::handleMenuItem)

            val offerAdapter = OfferAdapter(
                fragmentManager = supportFragmentManager,
                tracker = tracker,
                marketManager = marketManager,
                removeDiscount = model::removeDiscount
            )
            val documentAdapter = DocumentAdapter(
                trackClick = tracker::openOfferLink
            )
            val concatAdapter = ConcatAdapter(offerAdapter, documentAdapter)

            binding.offerScroll.adapter = concatAdapter

            model.viewState.observe(this@OfferActivity) { viewState ->
                when (viewState) {
                    OfferViewModel.ViewState.HasContracts -> startLoggedInActivity()
                    is OfferViewModel.ViewState.OfferItems -> {
                        offerAdapter.submitList(viewState.offerItems)
                        documentAdapter.submitList(viewState.documents)
                    }
                    is OfferViewModel.ViewState.Error.GeneralError -> showErrorDialog(
                        viewState.message ?: getString(R.string.home_tab_error_body)
                    )
                    is OfferViewModel.ViewState.Error -> showErrorDialog(getString(R.string.home_tab_error_body))
                }
            }

            signButton.setHapticClickListener {
                tracker.floatingSign()
                OfferSignDialog.newInstance().show(
                    supportFragmentManager,
                    OfferSignDialog.TAG
                )
            }
        }
    }

    private fun startLoggedInActivity() {
        storeBoolean(IS_VIEWING_OFFER, false)
        LoggedInActivity.newInstance(
            context = this,
            isFromOnboarding = true,
            withoutHistory = true
        )
    }

    private fun handleMenuItem(menuItem: MenuItem) = when (menuItem.itemId) {
        R.id.chat -> {
            tracker.openChat()
            model.triggerOpenChat {
                startClosableChat(true)
            }
            true
        }
        R.id.app_settings -> {
            tracker.settings()
            startActivity(SettingsActivity.newInstance(this))
            true
        }
        R.id.app_info -> {
            startActivity(MoreOptionsActivity.newInstance(this))
            true
        }
        R.id.login -> {
            marketManager.market?.openAuth(this, supportFragmentManager)
            true
        }
        R.id.restart -> {
            showRestartDialog()
            true
        }
        else -> false
    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.error_dialog_title)
            .setMessage(message)
            .setPositiveButton(R.string.ALERT_OK) { _, _ -> finish() }
            .show()
    }

    private fun showRestartDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_alert_restart_onboarding_title)
            .setMessage(R.string.settings_alert_restart_onboarding_description)
            .setPositiveButton(R.string.ALERT_OK) { _, _ ->
                // TODO
                Toast.makeText(this, "Not implemented", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton(R.string.ALERT_CANCEL) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun applyInsets(height: Int) {
        binding.offerScroll.updatePadding(top = height + scrollInitialPaddingTop)
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, OfferActivity::class.java)
    }
}
