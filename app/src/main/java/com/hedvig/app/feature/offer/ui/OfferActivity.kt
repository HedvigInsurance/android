package com.hedvig.app.feature.offer.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.transition.TransitionManager
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.carousell.concatadapterextension.ConcatItemDecoration
import com.carousell.concatadapterextension.ConcatSpanSizeLookup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationTitle
import com.hedvig.android.owldroid.type.SignMethod
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityOfferBinding
import com.hedvig.app.feature.documents.DocumentAdapter
import com.hedvig.app.feature.embark.ui.MoreOptionsActivity
import com.hedvig.app.feature.home.ui.changeaddress.result.ChangeAddressResultActivity
import com.hedvig.app.feature.insurablelimits.InsurableLimitsAdapter
import com.hedvig.app.feature.offer.OfferSignDialog
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.feature.offer.quotedetail.QuoteDetailActivity
import com.hedvig.app.feature.offer.ui.checkout.CheckoutActivity
import com.hedvig.app.feature.perils.PerilsAdapter
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.service.LoginStatus
import com.hedvig.app.util.extensions.showErrorDialog
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.view.hide
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class OfferActivity : BaseActivity(R.layout.activity_offer) {
    private val quoteIds: List<String>
        get() = intent.getStringArrayExtra(QUOTE_IDS)?.toList() ?: emptyList()
    private val model: OfferViewModel by viewModel { parametersOf(quoteIds) }
    private val binding by viewBinding(ActivityOfferBinding::bind)
    private val requestBuilder: RequestBuilder<PictureDrawable> by inject()
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

            val topOfferAdapter = OfferAdapter(
                fragmentManager = supportFragmentManager,
                tracker = tracker,
                marketManager = marketManager,
                openQuoteDetails = model::onOpenQuoteDetails,
                onRemoveDiscount = model::removeDiscount,
                onSign = ::onSign
            )
            val perilsAdapter = PerilsAdapter(
                fragmentManager = supportFragmentManager,
                requestBuilder = requestBuilder,
            )
            val insurableLimitsAdapter = InsurableLimitsAdapter(
                fragmentManager = supportFragmentManager
            )
            val documentAdapter = DocumentAdapter(
                trackClick = tracker::openOfferLink
            )
            val bottomOfferAdapter = OfferAdapter(
                fragmentManager = supportFragmentManager,
                tracker = tracker,
                marketManager = marketManager,
                openQuoteDetails = model::onOpenQuoteDetails,
                onRemoveDiscount = model::removeDiscount,
                onSign = ::onSign
            )

            val concatAdapter = ConcatAdapter(
                topOfferAdapter,
                perilsAdapter,
                insurableLimitsAdapter,
                documentAdapter,
                bottomOfferAdapter,
            )

            binding.offerScroll.adapter = concatAdapter
            binding.offerScroll.addItemDecoration(ConcatItemDecoration { concatAdapter.adapters })
            (binding.offerScroll.layoutManager as? GridLayoutManager)?.let { gridLayoutManager ->
                gridLayoutManager.spanSizeLookup =
                    ConcatSpanSizeLookup(gridLayoutManager.spanCount) { concatAdapter.adapters }
            }

            model
                .viewState
                .flowWithLifecycle(lifecycle)
                .onEach { viewState ->
                    when (viewState) {
                        is OfferViewModel.ViewState.Offer -> {
                            if (concatAdapter.itemCount == 0) {
                                scheduleEnterAnimation()
                            }
                            topOfferAdapter.submitList(viewState.topOfferItems)
                            perilsAdapter.submitList(viewState.perils)
                            insurableLimitsAdapter.submitList(viewState.insurableLimitsItems)
                            documentAdapter.submitList(viewState.documents)
                            bottomOfferAdapter.submitList(viewState.bottomOfferItems)
                            setSignState(viewState.signMethod)

                            TransitionManager.beginDelayedTransition(binding.offerToolbar)
                            when (viewState.title) {
                                QuoteBundleAppConfigurationTitle.LOGO -> binding.toolbarLogo.show()
                                QuoteBundleAppConfigurationTitle.UPDATE_SUMMARY,
                                QuoteBundleAppConfigurationTitle.UNKNOWN__ -> binding.toolbarTitle.show()
                            }
                            binding.progressBar.hide()
                            inflateMenu(viewState.loginStatus)
                        }
                        is OfferViewModel.ViewState.Loading -> {
                            binding.progressBar.show()
                        }
                    }
                }
                .launchIn(lifecycleScope)

            model
                .events
                .flowWithLifecycle(lifecycle)
                .onEach { event ->
                    when (event) {
                        is OfferViewModel.Event.Error -> showErrorDialog(
                            event.message ?: getString(R.string.home_tab_error_body)
                        ) { }
                        is OfferViewModel.Event.OpenQuoteDetails -> {
                            startActivity(
                                QuoteDetailActivity.newInstance(
                                    this@OfferActivity,
                                    event.quoteDetailItems
                                )
                            )
                        }
                        is OfferViewModel.Event.OpenCheckout -> {
                            startActivity(
                                CheckoutActivity.newInstance(
                                    this@OfferActivity,
                                    event.checkoutParameter
                                )
                            )
                        }
                        is OfferViewModel.Event.ApproveSuccessful -> {
                            startActivity(
                                ChangeAddressResultActivity.newInstance(
                                    this@OfferActivity,
                                    ChangeAddressResultActivity.Result.Success(event.moveDate)
                                )
                            )
                        }
                        OfferViewModel.Event.ApproveError -> {
                            startActivity(
                                ChangeAddressResultActivity.newInstance(
                                    this@OfferActivity,
                                    ChangeAddressResultActivity.Result.Error
                                )
                            )
                        }
                    }
                }
                .launchIn(lifecycleScope)

            signButton.setHapticClickListener {
                tracker.floatingSign()
                OfferSignDialog.newInstance().show(
                    supportFragmentManager,
                    OfferSignDialog.TAG
                )
            }
        }
    }

    private fun scheduleEnterAnimation() {
        binding.offerScroll.scheduleLayoutAnimation()
        binding.offerScroll.postOnAnimation {
            changeBackgroundWithDelay()
        }
    }

    // Some items in the concat adapter does not have a background. We want them
    // to have the default colorBackground. Before submitting items to the recycler view we
    // are showing a gradient for a smoother transition to the header item. Hence why we need to change
    // the background here.
    private fun changeBackgroundWithDelay() {
        lifecycleScope.launch {
            delay(800)
            binding.offerRoot.setBackgroundColor(binding.offerRoot.context.getColor(R.color.colorBackground))
        }
    }

    private fun inflateMenu(loginStatus: LoginStatus) {
        binding.offerToolbar.menu.clear()
        when (loginStatus) {
            LoginStatus.ONBOARDING,
            LoginStatus.IN_OFFER -> binding.offerToolbar.inflateMenu(R.menu.offer_menu)
            LoginStatus.LOGGED_IN -> binding.offerToolbar.inflateMenu(R.menu.offer_menu_logged_in)
        }
    }

    private fun setSignState(signMethod: SignMethod) {
        binding.signButton.bindWithSignMethod(signMethod)
        binding.signButton.setHapticClickListener {
            onSign(signMethod)
        }
    }

    private fun onSign(signMethod: SignMethod) {
        when (signMethod) {
            SignMethod.SWEDISH_BANK_ID -> {
                tracker.floatingSign()
                OfferSignDialog.newInstance().show(
                    supportFragmentManager,
                    OfferSignDialog.TAG
                )
            }
            SignMethod.SIMPLE_SIGN -> model.onOpenCheckout()
            SignMethod.APPROVE_ONLY -> model.approveOffer()
            SignMethod.NORWEGIAN_BANK_ID,
            SignMethod.DANISH_BANK_ID,
            SignMethod.UNKNOWN__ -> showErrorDialog("Could not parse sign method", ::finish)
        }
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
        private const val QUOTE_IDS = "QUOTE_IDS"
        fun newInstance(context: Context, quoteIds: List<String> = emptyList()) =
            Intent(context, OfferActivity::class.java).apply {
                putExtra(QUOTE_IDS, quoteIds.toTypedArray())
            }
    }
}
