package com.hedvig.app.feature.norway

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.observe
import kotlinx.android.synthetic.main.activity_norwegian_authentication.*
import org.koin.android.viewmodel.ext.android.viewModel

class NorwegianAuthenticationActivity : BaseActivity(R.layout.activity_norwegian_authentication) {
    private val model: NorwegianAuthenticationViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model.redirectUrl.observe(this) { redirectUrl ->
            redirectUrl?.let { ru ->
                norwegianBankIdContainer.loadUrl(ru)
            }
        }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, NorwegianAuthenticationActivity::class.java)
    }
}
