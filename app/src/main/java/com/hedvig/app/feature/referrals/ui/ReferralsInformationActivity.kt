package com.hedvig.app.feature.referrals.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.openUri
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.setupToolbarScrollListener
import com.hedvig.app.util.extensions.view.updatePadding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import e
import kotlinx.android.synthetic.main.activity_referrals_information.*

class ReferralsInformationActivity : BaseActivity(R.layout.activity_referrals_information) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val termsUrl = intent.getStringExtra(TERMS_URL)

        if (termsUrl == null) {
            e { "Programmer error: TERMS_URL not provided to ${this.javaClass.name}" }
            return
        }

        root.setEdgeToEdgeSystemUiFlags(true)

        toolbar.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
        }

        scrollView.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
        }

        scrollView.setupToolbarScrollListener(toolbar)

        val termsAsUri = Uri.parse(termsUrl)

        termsAndConditions.setHapticClickListener {
            if (canOpenUri(termsAsUri)) {
                openUri(termsAsUri)
            }
        }
    }

    companion object {
        private const val TERMS_URL = "TERMS_URL"
        fun newInstance(context: Context, termsUrl: String) =
            Intent(context, ReferralsInformationActivity::class.java).apply {
                putExtra(TERMS_URL, termsUrl)
            }
    }
}
