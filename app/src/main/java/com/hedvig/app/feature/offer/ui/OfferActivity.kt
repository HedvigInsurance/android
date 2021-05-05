package com.hedvig.app.feature.offer.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityOfferBinding
import com.hedvig.app.feature.embark.ui.MoreOptionsActivity
import com.hedvig.app.feature.embark.ui.TooltipBottomSheet
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.service.LoginStatusService.Companion.IS_VIEWING_OFFER
import com.hedvig.app.util.boundedColorLerp
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.storeBoolean
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.Float.min

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
            offerToolbar.doOnLayout { applyInsets(it.height) }

            offerToolbar.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
                applyInsets(view.height)
            }

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
                            colorAttr(android.R.attr.colorBackground),
                            percentage
                        )
                    )
                }
            })

            offerToolbar.setNavigationOnClickListener { onBackPressed() }
            offerToolbar.setOnMenuItemClickListener(::handleMenuItem)

            offerScroll.adapter = OfferAdapter(
                supportFragmentManager,
                tracker,
                marketManager
            ) {
                model.removeDiscount()
            }

            model.data.observe(this@OfferActivity) {
                it?.let { data ->
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
                            }
                        )
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
        fun newInstance(context: Context) = Intent(context, OfferActivity::class.java)
    }
}
