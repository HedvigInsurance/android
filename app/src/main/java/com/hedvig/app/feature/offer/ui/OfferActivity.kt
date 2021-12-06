package com.hedvig.app.feature.offer.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import android.view.MenuItem
import android.view.animation.AnimationUtils
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import com.carousell.concatadapterextension.ConcatItemDecoration
import com.carousell.concatadapterextension.ConcatSpanSizeLookup
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationTitle
import com.hedvig.android.owldroid.type.SignMethod
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.SplashActivity
import com.hedvig.app.authenticate.LoginStatus
import com.hedvig.app.databinding.ActivityOfferBinding
import com.hedvig.app.feature.crossselling.ui.CrossSellingResult
import com.hedvig.app.feature.crossselling.ui.CrossSellingResultActivity
import com.hedvig.app.feature.documents.DocumentAdapter
import com.hedvig.app.feature.embark.ui.MoreOptionsActivity
import com.hedvig.app.feature.home.ui.changeaddress.result.ChangeAddressResultActivity
import com.hedvig.app.feature.insurablelimits.InsurableLimitsAdapter
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.feature.offer.PostSignScreen
import com.hedvig.app.feature.offer.quotedetail.QuoteDetailActivity
import com.hedvig.app.feature.offer.ui.checkout.CheckoutActivity
import com.hedvig.app.feature.perils.PerilsAdapter
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.feature.swedishbankid.sign.SwedishBankIdSignDialog
import com.hedvig.app.ui.animator.ViewHolderReusingDefaultItemAnimator
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.showErrorDialog
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.view.applyNavigationBarInsetsMargin
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.hide
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class OfferActivity : BaseActivity(R.layout.activity_offer) {
    private val quoteIds: List<String>
        get() = intent.getStringArrayExtra(QUOTE_IDS)?.toList() ?: emptyList()
    private val shouldShowOnNextAppStart: Boolean
        get() = intent.getBooleanExtra(SHOULD_SHOW_ON_NEXT_APP_START, false)
    private val insurelyDataCollectionReferenceUuid: String?
        get() = intent.getStringExtra(INSURELY_DATA_COLLECTION_REFERENCE)

    private val model: OfferViewModel by viewModel {
        parametersOf(quoteIds, shouldShowOnNextAppStart, insurelyDataCollectionReferenceUuid)
    }
    private val binding by viewBinding(ActivityOfferBinding::bind)
    private val imageLoader: ImageLoader by inject()
    private val tracker: OfferTracker by inject()
    private val marketManager: MarketManager by inject()
    private var hasStartedRecyclerAnimation: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            window.compatSetDecorFitsSystemWindows(false)
            offerToolbar.applyStatusBarInsets()
            signButton.applyNavigationBarInsetsMargin()

            appbar.background.alpha = 0
            offerScroll.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                private var scrollY = 0
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    scrollY += dy
                    val percentage = scrollY.toFloat() / offerToolbar.height
                    appbar.background.alpha = (percentage * 40).toInt().coerceAtMost(255)

                    if (percentage >= 9) {
                        appbar.elevation = 5f
                    } else {
                        appbar.elevation = 0f
                    }

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
                onSign = ::onSign,
                reload = model::reload,
                openChat = ::openChat,
            )
            val perilsAdapter = PerilsAdapter(
                fragmentManager = supportFragmentManager,
                imageLoader = imageLoader
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
                onSign = ::onSign,
                reload = model::reload,
                openChat = ::openChat,
            )

            val concatAdapter = ConcatAdapter(
                topOfferAdapter,
                perilsAdapter,
                insurableLimitsAdapter,
                documentAdapter,
                bottomOfferAdapter,
            )

            binding.offerScroll.adapter = concatAdapter
            binding.offerScroll.itemAnimator = ViewHolderReusingDefaultItemAnimator()
            binding.offerScroll.addItemDecoration(ConcatItemDecoration { concatAdapter.adapters })
            (binding.offerScroll.layoutManager as? GridLayoutManager)?.let { gridLayoutManager ->
                gridLayoutManager.spanSizeLookup =
                    ConcatSpanSizeLookup(gridLayoutManager.spanCount) { concatAdapter.adapters }
            }

            model
                .viewState
                .flowWithLifecycle(lifecycle)
                .onEach { viewState ->
                    binding.progressBar.isVisible = viewState is OfferViewModel.ViewState.Loading
                    binding.offerScroll.isVisible = viewState !is OfferViewModel.ViewState.Loading
                    when (viewState) {
                        is OfferViewModel.ViewState.Loading -> {}
                        is OfferViewModel.ViewState.Error -> {
                            perilsAdapter.submitList(emptyList())
                            insurableLimitsAdapter.submitList(emptyList())
                            documentAdapter.submitList(emptyList())
                            bottomOfferAdapter.submitList(emptyList())
                            topOfferAdapter.submitList(listOf(OfferModel.Error))
                            binding.progressBar.isVisible = false
                            binding.offerScroll.isVisible = true
                        }
                        is OfferViewModel.ViewState.Content -> {
                            topOfferAdapter.submitList(viewState.topOfferItems)
                            perilsAdapter.submitList(viewState.perils)
                            insurableLimitsAdapter.submitList(viewState.insurableLimitsItems)
                            documentAdapter.submitList(viewState.documents)
                            bottomOfferAdapter.submitList(viewState.bottomOfferItems)
                            setSignButtonState(viewState.signMethod, viewState.checkoutLabel)

                            TransitionManager.beginDelayedTransition(binding.offerToolbar)
                            setTitleVisibility(viewState)
                            inflateMenu(viewState.loginStatus)

                            if (!hasStartedRecyclerAnimation) {
                                scheduleEnterAnimation()
                            }
                        }
                    }
                }
                .launchIn(lifecycleScope)

            model
                .events
                .flowWithLifecycle(lifecycle)
                .onEach { event ->
                    when (event) {
                        is OfferViewModel.Event.OpenQuoteDetails -> {
                            startActivity(
                                QuoteDetailActivity.newInstance(
                                    context = this@OfferActivity,
                                    title = event.quoteDetailItems.displayName,
                                    perils = event.quoteDetailItems.perils,
                                    insurableLimits = event.quoteDetailItems.insurableLimits,
                                    documents = event.quoteDetailItems.documents,
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
                            when (event.postSignScreen) {
                                PostSignScreen.CONNECT_PAYIN -> {
                                    marketManager
                                        .market
                                        ?.connectPayin(this@OfferActivity, true)
                                        ?.let { startActivity(it) }
                                }
                                PostSignScreen.MOVE -> {
                                    ChangeAddressResultActivity.newInstance(
                                        this@OfferActivity,
                                        ChangeAddressResultActivity.Result.Success(event.startDate),
                                    )
                                }
                                PostSignScreen.CROSS_SELL -> {
                                    startActivity(
                                        CrossSellingResultActivity.newInstance(
                                            this@OfferActivity,
                                            CrossSellingResult.Success.from(event)
                                        )
                                    )
                                }
                            }
                        }
                        is OfferViewModel.Event.ApproveError -> {
                            when (event.postSignScreen) {
                                PostSignScreen.CONNECT_PAYIN -> {
                                }
                                PostSignScreen.MOVE -> {
                                    startActivity(
                                        ChangeAddressResultActivity.newInstance(
                                            this@OfferActivity,
                                            ChangeAddressResultActivity.Result.Error
                                        )
                                    )
                                }
                                PostSignScreen.CROSS_SELL -> {
                                    startActivity(
                                        CrossSellingResultActivity.newInstance(
                                            this@OfferActivity,
                                            CrossSellingResult.Error
                                        )
                                    )
                                }
                            }
                        }
                        OfferViewModel.Event.DiscardOffer -> {
                            startActivity(
                                Intent(
                                    this@OfferActivity,
                                    SplashActivity::class.java
                                )
                            )
                        }
                        is OfferViewModel.Event.StartSwedishBankIdSign -> {
                            SwedishBankIdSignDialog
                                .newInstance(event.autoStartToken, event.quoteIds)
                                .show(supportFragmentManager, SwedishBankIdSignDialog.TAG)
                        }
                    }
                }
                .launchIn(lifecycleScope)
        }
    }

    private fun setTitleVisibility(viewState: OfferViewModel.ViewState.Content) {
        when (viewState.title) {
            QuoteBundleAppConfigurationTitle.LOGO -> {
                binding.toolbarLogo.isVisible = true
                binding.toolbarTitle.isVisible = false
            }
            QuoteBundleAppConfigurationTitle.UPDATE_SUMMARY,
            QuoteBundleAppConfigurationTitle.UNKNOWN__,
            -> {
                binding.toolbarTitle.isVisible = true
                binding.toolbarLogo.isVisible = false
            }
        }
    }

    private fun openChat() {
        lifecycleScope.launch {
            model.triggerOpenChat()
            startClosableChat(true)
        }
    }

    private fun scheduleEnterAnimation() {
        hasStartedRecyclerAnimation = true
        binding.offerScroll.scheduleLayoutAnimation()
        val animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_appear_from_bottom)
        binding.offerScroll.layoutAnimation = animation
    }

    private fun inflateMenu(loginStatus: LoginStatus) {
        val menu = binding.offerToolbar.menu
        menu.clear()
        when (loginStatus) {
            LoginStatus.ONBOARDING,
            LoginStatus.IN_OFFER,
            -> binding.offerToolbar.inflateMenu(R.menu.offer_menu)
            LoginStatus.LOGGED_IN -> {
                binding.offerToolbar.inflateMenu(R.menu.offer_menu_logged_in)
                menu.getItem(0).actionView.setOnClickListener {
                    handleMenuItem(menu[0])
                }
            }
        }
    }

    private fun setSignButtonState(signMethod: SignMethod, checkoutLabel: CheckoutLabel) {
        binding.signButton.text = checkoutLabel.toString(this)
        binding.signButton.icon = signMethod.checkoutIconRes()?.let(::compatDrawable)
        binding.signButton.setHapticClickListener {
            tracker.checkoutFloating(checkoutLabel.localizationKey(this))
            onSign(signMethod)
        }
    }

    private fun onSign(signMethod: SignMethod) {
        when (signMethod) {
            SignMethod.SWEDISH_BANK_ID -> model.onSwedishBankIdSign()
            SignMethod.SIMPLE_SIGN -> model.onOpenCheckout()
            SignMethod.APPROVE_ONLY -> model.approveOffer()
            SignMethod.NORWEGIAN_BANK_ID,
            SignMethod.DANISH_BANK_ID,
            SignMethod.UNKNOWN__,
            -> showErrorDialog("Could not parse sign method", ::finish)
        }
    }

    private fun handleMenuItem(menuItem: MenuItem) = when (menuItem.itemId) {
        R.id.chat -> {
            tracker.openChat()
            openChat()
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
        R.id.discard_offer -> {
            showAlert(
                title = R.string.OFFER_QUIT_TITLE,
                message = R.string.OFFER_QUIT_MESSAGE,
                positiveLabel = R.string.general_back_button,
                negativeLabel = R.string.general_discard_button,
                positiveAction = {},
                negativeAction = { model.onDiscardOffer() }
            )
            true
        }
        else -> false
    }

    companion object {
        private const val QUOTE_IDS = "QUOTE_IDS"
        private const val SHOULD_SHOW_ON_NEXT_APP_START = "SHOULD_SHOW_ON_NEXT_APP_START"
        private const val INSURELY_DATA_COLLECTION_REFERENCE =
            "com.hedvig.app.feature.offer.ui.INSURELY_DATA_COLLECTION_REFERENCE"

        fun newInstance(
            context: Context,
            quoteIds: List<String> = emptyList(),
            shouldShowOnNextAppStart: Boolean = false,
            insurelyDataCollectionReferenceUuid: String? = null,
        ) = Intent(context, OfferActivity::class.java).apply {
            putExtra(QUOTE_IDS, quoteIds.toTypedArray())
            putExtra(SHOULD_SHOW_ON_NEXT_APP_START, shouldShowOnNextAppStart)
            putExtra(INSURELY_DATA_COLLECTION_REFERENCE, insurelyDataCollectionReferenceUuid)
        }
    }
}
