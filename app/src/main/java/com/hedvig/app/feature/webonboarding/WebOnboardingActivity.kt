package com.hedvig.app.feature.webonboarding

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebMessage
import android.webkit.WebMessagePort
import android.webkit.WebView
import android.webkit.WebViewClient
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.profile.ui.payment.TrustlyActivity
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.makeUserAgent
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.activity_web_onboarding.*
import org.json.JSONObject

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

        val channels = webOnboarding.createWebMessageChannel()
        val recv = channels[0]
        val send = channels[1]

        recv.setWebMessageCallback { _, message ->
            if (message == null) {
                return@setWebMessageCallback
            }
            val json = JSONObject(message.data)
            if (json.optString("type") == "SIGN_COMPLETE") {
                startActivity(TrustlyActivity.newInstance(this, withExplainer = true, withoutHistory = true))
            }
        }

        webOnboarding.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                webOnboarding.postWebMessage(WebMessage("init", arrayOf(send)), Uri.EMPTY)
                webOnboarding.postWebMessage(WebMessage("{ \"type\":\"TOKEN\", \"payload\":\"123\"}"), Uri.EMPTY)
            }
        }

        webOnboarding.loadUrl("file:///android_asset/fake_web_onboarding.html") // TODO: Configurable URL. Maybe loaded from backend?

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

        fun WebMessagePort.setWebMessageCallback(action: (port: WebMessagePort?, message: WebMessage?) -> Unit) {
            this.setWebMessageCallback(object : WebMessagePort.WebMessageCallback() {
                override fun onMessage(port: WebMessagePort?, message: WebMessage?) {
                    action(port, message)
                }
            })
        }
    }
}
