package com.hedvig.app.feature.referrals.ui.editcode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.observe
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import e
import kotlinx.android.synthetic.main.activity_referrals_edit_code.*
import org.koin.android.viewmodel.ext.android.viewModel

class ReferralsEditCodeActivity : BaseActivity(R.layout.activity_referrals_edit_code) {
    private val model: ReferralsEditCodeViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root.setEdgeToEdgeSystemUiFlags(true)

        toolbar.doOnLayout { applyInsets(it.height) }

        toolbar.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
            applyInsets(view.height)
        }

        scrollView.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save -> {
                    // TODO: Check that everything is valid before making call
                    model.changeCode(code.text.toString())
                    true
                }
                else -> false
            }
        }

        val currentCode = intent.getStringExtra(CODE)

        if (currentCode == null) {
            e { "Programmer error: `CODE` not passed to ${this.javaClass.name}" }
        }

        code.setText(currentCode)

        model.data.observe(this) { data ->
            if (data == null) {
                return@observe
            }

            if (data.isFailure) {
                codeContainer.error = getString(R.string.referrals_change_code_sheet_general_error)
                return@observe
            }

            data.getOrNull()?.updateReferralCampaignCode?.let { urcc ->
                urcc.asSuccessfullyUpdatedCode?.let {
                    codeContainer.error = null
                    finish()
                    return@observe
                }
                urcc.asCodeAlreadyTaken?.let {
                    codeContainer.error =
                        getString(R.string.referrals_change_code_sheet_error_claimed_code)
                    return@observe
                }
            }
        }
    }

    private fun applyInsets(toolbarHeight: Int) {
        scrollView.updatePadding(top = toolbarHeight)
    }

    companion object {
        private const val CODE = "CODE"
        fun newInstance(context: Context, currentCode: String) =
            Intent(context, ReferralsEditCodeActivity::class.java).apply {
                putExtra(CODE, currentCode)
            }
    }
}
