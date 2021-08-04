package com.hedvig.app.feature.profile.ui.aboutapp

import android.os.Bundle
import androidx.core.view.WindowCompat
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityLicensesBinding
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.setupToolbarScrollListener
import com.hedvig.app.util.extensions.viewBinding

class LicensesActivity : BaseActivity(R.layout.activity_licenses) {
    private val binding by viewBinding(ActivityLicensesBinding::bind)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            window.compatSetDecorFitsSystemWindows(false)

            root.applyNavigationBarInsets()
            toolbar.applyStatusBarInsets()

            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
            root.setupToolbarScrollListener(toolbar)

            webView.loadUrl("file:///android_asset/open_source_licenses.html")
        }
    }
}
