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
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInResult
import com.carousell.concatadapterextension.ConcatItemDecoration
import com.carousell.concatadapterextension.ConcatSpanSizeLookup
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.SplashActivity
import com.hedvig.app.authenticate.LoginStatus
import com.hedvig.app.databinding.ActivityOfferBinding
import com.hedvig.app.feature.adyen.PaymentTokenId
import com.hedvig.app.feature.adyen.payin.startAdyenPayment
import com.hedvig.app.feature.checkout.CheckoutActivity
import com.hedvig.app.feature.crossselling.ui.CrossSellingResult
import com.hedvig.app.feature.crossselling.ui.CrossSellingResultActivity
import com.hedvig.app.feature.documents.DocumentAdapter
import com.hedvig.app.feature.embark.ui.MoreOptionsActivity
import com.hedvig.app.feature.embark.util.SelectedContractType
import com.hedvig.app.feature.home.ui.changeaddress.result.ChangeAddressResultActivity
import com.hedvig.app.feature.insurablelimits.InsurableLimitsAdapter
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.feature.offer.model.CheckoutLabel
import com.hedvig.app.feature.offer.model.CheckoutMethod
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.offer.model.checkoutIconRes
import com.hedvig.app.feature.offer.model.quotebundle.PostSignScreen
import com.hedvig.app.feature.offer.model.quotebundle.ViewConfiguration
import com.hedvig.app.feature.offer.quotedetail.QuoteDetailActivity
import com.hedvig.app.feature.payment.connectPayinIntent
import com.hedvig.app.feature.perils.PerilsAdapter
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.feature.swedishbankid.sign.SwedishBankIdSignDialog
import com.hedvig.app.getLocale
import com.hedvig.app.ui.animator.ViewHolderReusingDefaultItemAnimator
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.showErrorDialog
import com.hedvig.app.util.extensions.startChat
import com.hedvig.app.util.extensions.toArrayList
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

  private lateinit var concatAdapter: ConcatAdapter
  override val screenName = "offer"

  private val quoteCartId: QuoteCartId
    get() = intent.getParcelableExtra(QUOTE_CART_ID)
      ?: intent.getStringExtra(QUOTE_CART_ID)?.let { QuoteCartId(it) }
      ?: error("A quote cart ID must be passed into OfferActivity")
  private val selectedContractTypes: List<SelectedContractType>
    get() = intent.getParcelableArrayListExtra(SELECTED_CONTRACT_TYPES) ?: emptyList()
  private val shouldShowOnNextAppStart: Boolean
    get() = intent.getBooleanExtra(SHOULD_SHOW_ON_NEXT_APP_START, false)

  private val model: OfferViewModel by viewModel {
    parametersOf(quoteCartId, selectedContractTypes, shouldShowOnNextAppStart)
  }
  private val binding by viewBinding(ActivityOfferBinding::bind)
  private val imageLoader: ImageLoader by inject()
  private val marketManager: MarketManager by inject()
  private var hasStartedRecyclerAnimation: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window.compatSetDecorFitsSystemWindows(false)
    binding.offerToolbar.applyStatusBarInsets()
    binding.signButton.applyNavigationBarInsetsMargin()

    binding.appbar.background.alpha = 0
    binding.offerScroll.addOnScrollListener(
      object : RecyclerView.OnScrollListener() {
        private var scrollY = 0
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
          scrollY += dy
          val percentage = scrollY.toFloat() / binding.offerToolbar.height
          binding.appbar.background.alpha = (percentage * 40).toInt().coerceAtMost(255)

          if (percentage >= 9) {
            binding.appbar.elevation = 5f
          } else {
            binding.appbar.elevation = 0f
          }

          if (percentage > 4 && !binding.signButton.isVisible) {
            TransitionManager.beginDelayedTransition(binding.offerRoot)
            binding.signButton.show()
          } else if (percentage < 4 && binding.signButton.isVisible) {
            TransitionManager.beginDelayedTransition(binding.offerRoot)
            binding.signButton.hide()
          }
        }
      },
    )

    binding.offerToolbar.setNavigationOnClickListener { onBackPressed() }
    binding.offerToolbar.setOnMenuItemClickListener(::handleMenuItem)

    val locale = getLocale(this@OfferActivity, marketManager.market)
    val topOfferAdapter = OfferAdapter(
      fragmentManager = supportFragmentManager,
      locale = locale,
      openQuoteDetails = model::onOpenQuoteDetails,
      onRemoveDiscount = model::removeDiscount,
      onSign = ::onSign,
      reload = model::reload,
      openChat = ::openChat,
    )
    val perilsAdapter = PerilsAdapter(
      fragmentManager = supportFragmentManager,
      imageLoader = imageLoader,
    )
    val insurableLimitsAdapter = InsurableLimitsAdapter(
      fragmentManager = supportFragmentManager,
    )
    val documentAdapter = DocumentAdapter()
    val bottomOfferAdapter = OfferAdapter(
      fragmentManager = supportFragmentManager,
      locale = locale,
      openQuoteDetails = model::onOpenQuoteDetails,
      onRemoveDiscount = model::removeDiscount,
      onSign = ::onSign,
      reload = model::reload,
      openChat = ::openChat,
    )

    concatAdapter = ConcatAdapter(
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
          is OfferViewModel.ViewState.Error -> showErrorDialog(
            viewState.message ?: getString(R.string.NETWORK_ERROR_ALERT_MESSAGE),
          ) {}
          is OfferViewModel.ViewState.Content -> {
            topOfferAdapter.submitList(viewState.createTopOfferItems())
            perilsAdapter.submitList(viewState.createPerilItems())
            insurableLimitsAdapter.submitList(viewState.createInsurableLimitItems())
            documentAdapter.submitList(viewState.createDocumentItems())
            bottomOfferAdapter.submitList(viewState.createBottomOfferItems())
            setSignButtonState(
              viewState.offerModel.checkoutMethod,
              viewState.bundleVariant.bundle.checkoutLabel,
              viewState.paymentMethods,
            )

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
          is OfferViewModel.Event.OpenQuoteDetails -> startQuoteDetailsActivity(event)
          is OfferViewModel.Event.OpenCheckout -> startCheckoutActivity(event)
          is OfferViewModel.Event.ApproveSuccessful -> handlePostSign(event)
          is OfferViewModel.Event.ApproveError -> handlePostSignError(event)
          OfferViewModel.Event.DiscardOffer -> startSplashActivity()
          OfferViewModel.Event.StartSwedishBankIdSign -> showSignDialog()
          OfferViewModel.Event.OpenChat -> startChat()
        }
      }
      .launchIn(lifecycleScope)
  }

  private fun setSignButtonState(
    checkoutMethod: CheckoutMethod,
    checkoutLabel: CheckoutLabel,
    paymentMethods: PaymentMethodsApiResponse?,
  ) {
    binding.signButton.text = checkoutLabel.toString(this)
    binding.signButton.icon = checkoutMethod.checkoutIconRes()?.let(::compatDrawable)
    binding.signButton.setHapticClickListener {
      onSign(checkoutMethod, paymentMethods)
    }
  }

  private fun showSignDialog() {
    SwedishBankIdSignDialog
      .newInstance(quoteCartId)
      .show(supportFragmentManager, SwedishBankIdSignDialog.TAG)
  }

  private fun startSplashActivity() {
    startActivity(Intent(this@OfferActivity, SplashActivity::class.java))
  }

  private fun handlePostSignError(event: OfferViewModel.Event.ApproveError) {
    when (event.postSignScreen) {
      PostSignScreen.CONNECT_PAYIN -> {
      }
      PostSignScreen.MOVE -> {
        startActivity(
          ChangeAddressResultActivity.newInstance(
            this@OfferActivity,
            ChangeAddressResultActivity.Result.Error,
          ),
        )
      }
      PostSignScreen.CROSS_SELL -> {
        startActivity(
          CrossSellingResultActivity.newInstance(
            this@OfferActivity,
            CrossSellingResult.Error,
          ),
        )
      }
    }
  }

  private fun handlePostSign(event: OfferViewModel.Event.ApproveSuccessful) {
    when (event.postSignScreen) {
      PostSignScreen.CONNECT_PAYIN -> {
        val market = marketManager.market ?: return
        startActivity(
          connectPayinIntent(
            this,
            event.payinType,
            market,
            true,
          ),
        )
      }
      PostSignScreen.MOVE -> {
        startActivity(
          ChangeAddressResultActivity.newInstance(
            this@OfferActivity,
            ChangeAddressResultActivity.Result.Success(event.startDate),
          ),
        )
      }
      PostSignScreen.CROSS_SELL -> {
        startActivity(
          CrossSellingResultActivity.newInstance(
            this@OfferActivity,
            CrossSellingResult.Success.from(event),
          ),
        )
      }
    }
  }

  private fun startCheckoutActivity(event: OfferViewModel.Event.OpenCheckout) {
    startActivity(CheckoutActivity.newInstance(this@OfferActivity, event.checkoutParameter))
  }

  private fun startQuoteDetailsActivity(event: OfferViewModel.Event.OpenQuoteDetails) {
    startActivity(
      QuoteDetailActivity.newInstance(
        context = this@OfferActivity,
        title = event.quoteDetailItems.displayName,
        perils = event.quoteDetailItems.perils,
        insurableLimits = event.quoteDetailItems.insurableLimits,
        documents = event.quoteDetailItems.documents,
      ),
    )
  }

  private fun setTitleVisibility(viewState: OfferViewModel.ViewState.Content) {
    when (viewState.bundleVariant.bundle.viewConfiguration.title) {
      ViewConfiguration.Title.LOGO -> {
        binding.toolbarLogo.isVisible = true
        binding.toolbarTitle.isVisible = false
      }
      ViewConfiguration.Title.UPDATE,
      ViewConfiguration.Title.UNKNOWN,
      -> {
        binding.toolbarTitle.isVisible = true
        binding.toolbarLogo.isVisible = false
      }
    }
  }

  private fun openChat() {
    lifecycleScope.launch {
      model.triggerOpenChat()
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
      LoginStatus.Onboarding,
      is LoginStatus.InOffer,
      -> binding.offerToolbar.inflateMenu(R.menu.offer_menu)
      LoginStatus.LoggedIn -> {
        binding.offerToolbar.inflateMenu(R.menu.offer_menu_logged_in)
        menu.getItem(0).actionView.setOnClickListener {
          handleMenuItem(menu[0])
        }
      }
    }
  }

  private fun onSign(checkoutMethod: CheckoutMethod, paymentMethods: PaymentMethodsApiResponse?) {
    when (checkoutMethod) {
      CheckoutMethod.SWEDISH_BANK_ID -> model.onSwedishBankIdSign()
      CheckoutMethod.SIMPLE_SIGN -> {
        if (paymentMethods != null) {
          startAdyenPayment(marketManager.market, paymentMethods)
        } else {
          model.onOpenCheckout()
        }
      }
      CheckoutMethod.APPROVE_ONLY -> model.approveOffer()
      CheckoutMethod.NORWEGIAN_BANK_ID,
      CheckoutMethod.DANISH_BANK_ID,
      CheckoutMethod.UNKNOWN,
      -> showErrorDialog("Could not parse sign method", ::finish)
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    @Suppress("DEPRECATION") // Replace with new result API when adyens handleActivityResult is updated
    super.onActivityResult(requestCode, resultCode, data)

    when (val result = DropIn.handleActivityResult(requestCode, resultCode, data)) {
      is DropInResult.CancelledByUser -> {}
      is DropInResult.Error -> showErrorDialog("Could not connect payment") {}
      is DropInResult.Finished -> {
        model.onPaymentTokenIdReceived(PaymentTokenId(result.result))
      }
      else -> {}
    }
  }

  private fun handleMenuItem(menuItem: MenuItem) = when (menuItem.itemId) {
    R.id.chat -> {
      openChat()
      true
    }
    R.id.app_settings -> {
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
        negativeAction = { model.onDiscardOffer() },
      )
      true
    }
    else -> false
  }

  companion object {
    private const val QUOTE_CART_ID = "QUOTE_CART_ID"
    private const val SELECTED_CONTRACT_TYPES = "SELECTED_TYPES"
    private const val SHOULD_SHOW_ON_NEXT_APP_START = "SHOULD_SHOW_ON_NEXT_APP_START"

    fun newInstance(
      context: Context,
      quoteCartId: QuoteCartId,
      selectedContractTypes: List<SelectedContractType> = emptyList(),
      shouldShowOnNextAppStart: Boolean = false,
    ) = Intent(
      context,
      OfferActivity::class.java,
    ).apply {
      putExtra(QUOTE_CART_ID, quoteCartId)
      putExtra(SELECTED_CONTRACT_TYPES, selectedContractTypes.toArrayList())
      putExtra(SHOULD_SHOW_ON_NEXT_APP_START, shouldShowOnNextAppStart)
    }
  }
}
