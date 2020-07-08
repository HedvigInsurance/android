package com.hedvig.app.feature.offer.ui

import android.content.Intent
import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.service.LoginStatusService.Companion.IS_VIEWING_OFFER
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.storeBoolean
import com.hedvig.app.util.extensions.view.fadeIn
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.updatePadding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import kotlinx.android.synthetic.main.activity_offer.*
import kotlinx.android.synthetic.main.loading_spinner.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class OfferActivity : BaseActivity(R.layout.activity_offer) {

    private val offerViewModel: OfferViewModel by viewModel()
    private val tracker: OfferTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        offerRoot.setEdgeToEdgeSystemUiFlags(true)
        offerToolbar.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
        }
        setSupportActionBar(offerToolbar)

        offerScroll.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(
                bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom
            )
        }

        offerScroll.adapter = OfferAdapter(
            supportFragmentManager,
            tracker
        ) {
            offerViewModel.removeDiscount()
        }

        offerViewModel.data.observe(lifecycleOwner = this) {
            it?.let { data ->
                loadingSpinner.remove()

                data.lastQuoteOfMember.asCompleteQuote?.quoteDetails?.asSwedishHouseQuoteDetails?.street

                if (data.contracts.isNotEmpty()) {
                    storeBoolean(IS_VIEWING_OFFER, false)
                    startActivity(Intent(this, LoggedInActivity::class.java).apply {
                        putExtra(LoggedInActivity.EXTRA_IS_FROM_ONBOARDING, true)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    })
                } else {
                    (offerScroll.adapter as? OfferAdapter)?.items = listOf(
                        OfferModel.Header(data),
                        OfferModel.Info,
                        OfferModel.Facts(data),
                        OfferModel.Perils(data),
                        OfferModel.Terms(data),
                        OfferModel.Footer
                    )
                }
            }
        }

        settings.setHapticClickListener {
            tracker.settings()
            startActivity(SettingsActivity.newInstance(this))
        }

        offerChatButton.setHapticClickListener {
            tracker.openChat()
            offerViewModel.triggerOpenChat {
                startClosableChat(true)
            }
        }
    }

    private fun bindToolBar(address: String) {
        offerToolbarAddress.text = address
        offerToolbar.fadeIn()
    }

    private fun initializeToolbar() {
/*
        offerScroll.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            val positionInSpan =
                scrollY - (BASE_MARGIN_OCTUPLE - (offerToolbar.height.toFloat()))
            val percentage = positionInSpan / offerToolbar.height

            if (percentage < -1 || percentage > 2) {
                return@setOnScrollChangeListener
            }

            offerToolbar.setBackgroundColor(
                boundedColorLerp(
                    Color.TRANSPARENT,
                    compatColor(R.color.translucent_tool_bar),
                    percentage
                )
            )
        }
*/
    }

    companion object {

    }
}
