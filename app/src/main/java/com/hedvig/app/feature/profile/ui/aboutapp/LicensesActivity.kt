package com.hedvig.app.feature.profile.ui.aboutapp

import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.setupLargeTitle
import kotlinx.android.synthetic.main.activity_licenses.*

class LicensesActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_licenses)

        setupLargeTitle(R.string.PROFILE_LICENSE_ATTRIBUTIONS_TITLE, R.drawable.ic_back) {
            onBackPressed()
        }
        webView.loadUrl("file:///android_asset/open_source_licenses.html")
    }
}
