package com.hedvig.app.feature.webonboarding

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.HttpAuthHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.BaseActivity
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.profile.ui.payment.connect.ConnectPaymentActivity
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.makeUserAgent
import com.hedvig.app.util.apollo.defaultLocale
import com.hedvig.app.util.extensions.getAuthenticationToken
import com.hedvig.app.util.extensions.setIsLoggedIn
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.activity_web_onboarding.*
import java.net.URLEncoder

class WebOnboardingActivity : BaseActivity(R.layout.activity_web_onboarding) {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        openSettings.setHapticClickListener {
            startActivity(SettingsActivity.newInstance(this))
        }

        openChat.setHapticClickListener {
            startActivity(ChatActivity.newInstance(this, showClose = true))
        }

        webOnboarding.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            domStorageEnabled = true
            userAgentString = makeUserAgent(this@WebOnboardingActivity)
        }

        webOnboarding.webViewClient = object : WebViewClient() {
            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                if (url?.contains("connect-payment") == true) {
                    view?.stopLoading()
                    setIsLoggedIn(true)
                    startActivity(
                        ConnectPaymentActivity.newInstance(
                            this@WebOnboardingActivity,
                            withExplainer = true,
                            withoutHistory = true
                        )
                    )
                    return
                }
                super.doUpdateVisitedHistory(view, url, isReload)
            }

            override fun onReceivedHttpAuthRequest(
                view: WebView?,
                handler: HttpAuthHandler?,
                host: String?,
                realm: String?
            ) {
                handler?.proceed("hedvig", "hedvig1234")
            }
        }

        val encodedToken = URLEncoder.encode(getAuthenticationToken(), UTF_8)

        val localePath = when (defaultLocale(this)) {
            Locale.NB_NO -> "no/"
            Locale.EN_NO -> "no-en/"
            Locale.DA_DK -> "dk/"
            Locale.EN_DK -> "dk-en/"
            else -> "no/"
        }

        webOnboarding.loadUrl("${BuildConfig.WEB_BASE_URL}${localePath}new-member?variation=android#token=${encodedToken}")
    }

    override fun onDestroy() {
        (webOnboarding.parent as ViewGroup).removeView(webOnboarding)
        webOnboarding.removeAllViews()
        webOnboarding.destroy()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (webOnboarding.canGoBack()) {
            webOnboarding.goBack()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val UTF_8 = "UTF-8"
       
        fun newInstance(context: Context) = Intent(context, WebOnboardingActivity::class.java)
    }
}
