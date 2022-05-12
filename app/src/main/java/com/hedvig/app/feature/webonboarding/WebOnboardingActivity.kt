package com.hedvig.app.feature.webonboarding

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.HttpAuthHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialFadeThrough
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.databinding.ActivityWebOnboardingBinding
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.makeUserAgent
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.extensions.startChat
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.ext.android.inject
import java.net.URLEncoder

class WebOnboardingActivity : BaseActivity(R.layout.activity_web_onboarding) {
    private val binding by viewBinding(ActivityWebOnboardingBinding::bind)
    private val marketManager: MarketManager by inject()
    private val localeManager: LocaleManager by inject()
    private val loginStatusService: LoginStatusService by inject()
    private val authenticationTokenService: AuthenticationTokenService by inject()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            openSettings.setHapticClickListener {
                startActivity(SettingsActivity.newInstance(this@WebOnboardingActivity))
            }

            openChat.setHapticClickListener {
                startChat()
            }

            webOnboarding.settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                domStorageEnabled = true
                userAgentString = makeUserAgent(this@WebOnboardingActivity, marketManager.market)
            }

            webOnboarding.webViewClient = object : WebViewClient() {
                override fun doUpdateVisitedHistory(
                    view: WebView?,
                    url: String?,
                    isReload: Boolean,
                ) {
                    if (url?.contains("connect-payment") == true) {
                        view?.stopLoading()
                        loginStatusService.isLoggedIn = true
                        marketManager.market?.connectPayin(
                            this@WebOnboardingActivity,
                            isPostSign = true
                        )?.let { startActivity(it) }
                        return
                    }
                    super.doUpdateVisitedHistory(view, url, isReload)
                }

                override fun onReceivedHttpAuthRequest(
                    view: WebView?,
                    handler: HttpAuthHandler?,
                    host: String?,
                    realm: String?,
                ) {
                    handler?.proceed("hedvig", "hedvig1234")
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    if (loadingIndicator.isVisible) {
                        TransitionManager.beginDelayedTransition(container, MaterialFadeThrough())

                        loadingIndicator.isVisible = false
                        webOnboarding.isVisible = true
                    }
                }
            }

            if (savedInstanceState == null) {
                webOnboarding.clearCache(true)
                CookieManager.getInstance().removeAllCookies {
                    load()
                }
            } else {
                load()
            }
        }
    }

    fun load() = with(binding) {
        val localePath = when (localeManager.defaultLocale()) {
            Locale.DA_DK -> "/dk"
            Locale.EN_DK -> "/dk-en"
            else -> "/dk"
        }

        val encodedToken = URLEncoder.encode(authenticationTokenService.authenticationToken, UTF_8)
        val webBaseUrl = getString(R.string.WEB_BASE_URL)

        webOnboarding.loadUrl("${webBaseUrl}$localePath/new-member?variation=android#token=$encodedToken")
    }

    override fun onDestroy() {
        binding.apply {
            (webOnboarding.parent as ViewGroup).removeView(webOnboarding)
            webOnboarding.removeAllViews()
            webOnboarding.destroy()
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        binding.apply {
            if (webOnboarding.canGoBack()) {
                webOnboarding.goBack()
            } else {
                super.onBackPressed()
            }
        }
    }

    companion object {
        private const val UTF_8 = "UTF-8"

        fun newInstance(context: Context) = Intent(context, WebOnboardingActivity::class.java)
    }
}
