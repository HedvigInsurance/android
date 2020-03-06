package com.hedvig.app.feature.webonboarding

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.activity_web_onboarding.*

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
        }

        webOnboarding.loadUrl("https://www.dev.hedvigit.com/new-member") // TODO: Configurable URL. Maybe loaded from backend?
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
