package com.hedvig.app.feature.marketing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.authenticate.LoginDialog
import com.hedvig.app.feature.marketing.marketpicked.MarketPickedScreen
import com.hedvig.app.feature.marketing.pickmarket.PickMarketScreen
import com.hedvig.app.feature.marketing.ui.BackgroundImage
import com.hedvig.app.feature.zignsec.SimpleSignAuthenticationActivity
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.setThemeOverlay
import com.hedvig.hanalytics.LoginMethod
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MarketingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.compatSetDecorFitsSystemWindows(false)
        val viewModel = getViewModel<MarketingViewModelNew>()
        setThemeOverlay(R.style.ThemeOverlay_Hedvig_MarketingActivity)
        setContent {
            HedvigTheme {
                val background by viewModel.background.collectAsState()
                BackgroundImage(background) {
                    val state by viewModel.state.collectAsState()
                    when (val s = state) {
                        Loading, MarketPicked.Loading -> CircularProgressIndicator(
                            Modifier.align(Alignment.Center)
                        )
                        is MarketPicked.Loaded -> MarketPickedScreen(
                            onClickMarket = viewModel::goToMarketPicker,
                            onClickSignUp = {
                                viewModel.onClickSignUp()
                                s.selectedMarket.openOnboarding(this@MarketingActivity, true)
                            },
                            onClickLogIn = {
                                viewModel.onClickLogIn()
                                when (s.loginMethod) {
                                    LoginMethod.BANK_ID_SWEDEN -> LoginDialog().show(
                                        supportFragmentManager,
                                        LoginDialog.TAG
                                    )
                                    LoginMethod.NEM_ID, LoginMethod.BANK_ID_NORWAY -> {
                                        startActivity(
                                            SimpleSignAuthenticationActivity.newInstance(
                                                this@MarketingActivity,
                                                s.selectedMarket
                                            )
                                        )
                                    }
                                    LoginMethod.OTP -> {
                                        // Not implemented
                                    }
                                }
                            },
                            data = s,
                        )
                        is PickMarket -> {
                            if (s.isLoading) {
                                CircularProgressIndicator(
                                    Modifier.align(Alignment.Center)
                                )
                            } else {
                                PickMarketScreen(
                                    onSubmit = viewModel::submitMarketAndLanguage,
                                    onSelectMarket = viewModel::setMarket,
                                    onSelectLanguage = viewModel::setLanguage,
                                    data = s,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(context: Context, withoutHistory: Boolean = false) =
            Intent(context, MarketingActivity::class.java).apply {
                if (withoutHistory) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
            }
    }
}
