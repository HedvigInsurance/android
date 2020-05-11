package com.hedvig.app.feature.profile.ui.aboutapp

import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.setupToolbarScrollListener
import com.hedvig.app.util.extensions.view.updatePadding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import kotlinx.android.synthetic.main.activity_licenses.*

class LicensesActivity : BaseActivity(R.layout.activity_licenses) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root.setEdgeToEdgeSystemUiFlags(true)
        root.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
        }
        toolbar.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        root.setupToolbarScrollListener(toolbar)

        webView.loadUrl("file:///android_asset/open_source_licenses.html")
    }
}
