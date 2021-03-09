package com.hedvig.app.feature.home.ui.changeaddress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ChangeAddressActivityBinding
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.Insetter
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags

class ChangeAddressActivity : BaseActivity(R.layout.change_address_activity) {

    private val binding by viewBinding(ChangeAddressActivityBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(binding) {
            root.setEdgeToEdgeSystemUiFlags(true)

            Insetter.builder().setOnApplyInsetsListener { view, insets, initialState ->
                view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
            }.applyToView(toolbar)

            Insetter.builder().setOnApplyInsetsListener { view, insets, initialState ->
                view.updateMargin(bottom = initialState.paddings.bottom + insets.stableInsetBottom)
            }.applyToView(continueButton)

            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }

            continueButton.setHapticClickListener {
                // TODO Start embark for moving flow
            }
        }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, ChangeAddressActivity::class.java)
    }
}