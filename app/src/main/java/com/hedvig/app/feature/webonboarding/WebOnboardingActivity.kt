package com.hedvig.app.feature.webonboarding

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.makeUserAgent
import com.hedvig.app.util.extensions.getAuthenticationToken
import com.hedvig.app.util.extensions.makeToast
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.activity_web_onboarding.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets.UTF_8

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
                    makeToast("Should open adyen!")
                    return
                }
                super.doUpdateVisitedHistory(view, url, isReload)
            }
        }

        val encodedToken = URLEncoder.encode(getAuthenticationToken(), UTF_8.toString())

        webOnboarding.loadUrl("file:///android_asset/fake_web_onboarding.html#token=${encodedToken}") // TODO: Configurable URL. Maybe loaded from backend?

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
        fun newInstance(context: Context) = Intent(context, WebOnboardingActivity::class.java)
    }
}
