package com.hedvig.app.feature.offer.ui.checkout

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityCheckoutBinding
import com.hedvig.app.util.extensions.setMarkdownText
import com.hedvig.app.util.extensions.viewBinding

class CheckoutActivity : BaseActivity(R.layout.activity_checkout) {

    private val binding by viewBinding(ActivityCheckoutBinding::bind)
    private val parameter by lazy {
        intent.getParcelableExtra<CheckoutParameter>(PARAMETER)
            ?: throw IllegalArgumentException("No parameter found for ${this.javaClass.simpleName}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            toolbar.setNavigationOnClickListener { onBackPressed() }
            title.text = parameter.title
            subtitle.text = parameter.subtitle
            val link = getString(R.string.OFFER_FOOTER_GDPR_INFO, parameter.gdprUrl)
            text.setMarkdownText(link)
        }
    }

    companion object {

        private const val PARAMETER = "PARAMETER"

        fun newInstance(context: Context, parameter: CheckoutParameter): Intent {
            return Intent(context, CheckoutActivity::class.java)
                .putExtra(PARAMETER, parameter)
        }
    }
}
