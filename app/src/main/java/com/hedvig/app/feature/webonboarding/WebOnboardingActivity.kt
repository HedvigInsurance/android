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
import com.hedvig.app.databinding.ActivityWebOnboardingBinding
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.makeUserAgent
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.extensions.getAuthenticationToken
import com.hedvig.app.util.extensions.setIsLoggedIn
import com.hedvig.app.util.extensions.toArrayList
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.ext.android.inject
import java.net.URLEncoder

class WebOnboardingActivity : BaseActivity(R.layout.activity_web_onboarding) {
    private val binding by viewBinding(ActivityWebOnboardingBinding::bind)
    private val marketManager: MarketManager by inject()
    private val localeManager: LocaleManager by inject()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            openSettings.setHapticClickListener {
                startActivity(SettingsActivity.newInstance(this@WebOnboardingActivity))
            }

            openChat.setHapticClickListener {
                startActivity(
                    ChatActivity.newInstance(
                        this@WebOnboardingActivity,
                        showClose = true
                    )
                )
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
                        setIsLoggedIn(true)
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
            Locale.NB_NO -> "/no"
            Locale.EN_NO -> "/no-en"
            Locale.DA_DK -> "/dk"
            Locale.EN_DK -> "/dk-en"
            else -> "/no"
        }

        val encodedToken = URLEncoder.encode(getAuthenticationToken(), UTF_8)

        val webBaseUrl = getString(R.string.WEB_BASE_URL)

        when (marketManager.market) {
            Market.NO -> {
                val isOffer = intent.getBooleanExtra(OFFER, false)
                if (isOffer) {
                    val keys = intent.getStringArrayListExtra(QUOTE_ID)
                        ?: throw IllegalArgumentException("No keys for offer!")

                    val encodedQuoteIDs = keys.joinToString(
                        separator = ",",
                        prefix = "[",
                        postfix = "]"
                    ).let {
                        URLEncoder.encode(it, UTF_8)
                    }

                    webOnboarding.loadUrl(
                        "${webBaseUrl}$localePath" +
                            "/new-member/offer?variation=android&quoteIds=$encodedQuoteIDs#token=$encodedToken"
                    )
                } else {
                    val webPath = intent.getStringExtra(WEB_PATH)
                    webOnboarding.loadUrl(
                        "${webBaseUrl}$webPath/start?variation=android#token=$encodedToken"
                    )
                }
            }
            Market.DK -> webOnboarding.loadUrl(
                "${webBaseUrl}$localePath/new-member?variation=android#token=$encodedToken"
            )
            else -> webOnboarding.loadUrl(
                "${webBaseUrl}$localePath/new-member?variation=android#token=$encodedToken"
            )
        }
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
        private const val WEB_PATH = "WEB_PATH"
        internal const val OFFER = "OFFER"
        private const val QUOTE_ID = "QUOTE_ID"

        fun newNoInstance(
            context: Context,
            webPath: String?,
            offer: Boolean = false,
            quoteId: List<String>,
        ): Intent {
            val intent = Intent(context, WebOnboardingActivity::class.java)
            intent.putExtra(WEB_PATH, webPath)
            intent.putExtra(OFFER, offer)
            intent.putExtra(QUOTE_ID, quoteId.toArrayList())
            return intent
        }

        fun newInstance(context: Context) = Intent(context, WebOnboardingActivity::class.java)
    }
}
