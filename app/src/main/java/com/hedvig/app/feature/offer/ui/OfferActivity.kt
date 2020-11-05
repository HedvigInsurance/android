package com.hedvig.app.feature.offer.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityOfferBinding
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.service.LoginStatusService.Companion.IS_VIEWING_OFFER
import com.hedvig.app.util.boundedColorLerp
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.storeBoolean
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class OfferActivity : BaseActivity(R.layout.activity_offer) {
    private val model: OfferViewModel by viewModel()
    private val binding by viewBinding(ActivityOfferBinding::bind)
    private val tracker: OfferTracker by inject()

    private var scrollInitialPaddingTop = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            offerRoot.setEdgeToEdgeSystemUiFlags(true)
            scrollInitialPaddingTop = offerScroll.paddingTop
            offerToolbar.doOnLayout { applyInsets(it.height) }

            offerToolbar.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
                applyInsets(view.height)
            }
            setSupportActionBar(offerToolbar)

            offerScroll.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
            }

            offerScroll.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                private var scrollY = 0
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    scrollY += dy

                    val percentage = scrollY.toFloat() / offerToolbar.height

                    offerToolbar.setBackgroundColor(
                        boundedColorLerp(
                            Color.TRANSPARENT,
                            compatColor(R.color.translucent_tool_bar),
                            percentage
                        )
                    )
                }
            })

            offerScroll.adapter = OfferAdapter(
                supportFragmentManager,
                tracker
            ) {
                model.removeDiscount()
            }

            model.data.observe(this@OfferActivity) {
                it?.let { data ->
                    data.lastQuoteOfMember.asCompleteQuote?.street?.let { street ->
                        offerToolbarAddress.text = street
                    }

                    if (data.contracts.isNotEmpty()) {
                        storeBoolean(IS_VIEWING_OFFER, false)
                        startActivity(
                            Intent(
                                this@OfferActivity,
                                LoggedInActivity::class.java
                            ).apply {
                                putExtra(LoggedInActivity.EXTRA_IS_FROM_ONBOARDING, true)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            })
                    } else {
                        (offerScroll.adapter as? OfferAdapter)?.submitList(
                            listOfNotNull(
                                OfferModel.Header(data),
                                OfferModel.Info,
                                OfferModel.Facts(data),
                                OfferModel.Perils(data),
                                OfferModel.Terms(data),
                                data.lastQuoteOfMember.asCompleteQuote?.currentInsurer?.let { currentInsurer ->
                                    if (currentInsurer.switchable == true) {
                                        OfferModel.Switcher(currentInsurer.displayName)
                                    } else {
                                        null
                                    }
                                },
                                OfferModel.Footer
                            )
                        )
                    }
                }
            }

            settings.setHapticClickListener {
                tracker.settings()
                startActivity(SettingsActivity.newInstance(this@OfferActivity))
            }

            offerChatButton.setHapticClickListener {
                tracker.openChat()
                model.triggerOpenChat {
                    startClosableChat(true)
                }
            }
        }
    }

    private fun applyInsets(height: Int) {
        binding.offerScroll.updatePadding(top = height + scrollInitialPaddingTop)
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, OfferActivity::class.java)

        val OfferQuery.AsCompleteQuote.street: String?
            get() {
                quoteDetails.asSwedishApartmentQuoteDetails?.street?.let { return it }
                quoteDetails.asSwedishHouseQuoteDetails?.street?.let { return it }
                return null
            }
    }
}
