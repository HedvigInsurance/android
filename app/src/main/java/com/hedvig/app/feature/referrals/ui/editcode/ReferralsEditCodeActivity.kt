package com.hedvig.app.feature.referrals.ui.editcode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.onChange
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import e
import kotlinx.android.synthetic.main.activity_referrals_edit_code.*
import org.koin.android.viewmodel.ext.android.viewModel

class ReferralsEditCodeActivity : BaseActivity(R.layout.activity_referrals_edit_code) {
    private val model: ReferralsEditCodeViewModel by viewModel()

    private var isSubmitting = false

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
                    if (isSubmitting) {
                        return@setOnMenuItemClickListener true
                    }
                    val enteredCode = code.text.toString()
                    if (validate(enteredCode) == ValidationResult.VALID) {
                        model.changeCode(enteredCode)
                    }
                    true
                }
                else -> false
            }
        }

        val currentCode = intent.getStringExtra(CODE)

        if (currentCode == null) {
            e { "Programmer error: `CODE` not passed to ${this.javaClass.name}" }
        }

        code.onChange { newValue ->
            when (validate(newValue)) {
                ValidationResult.VALID -> {
                    toolbar.menu.findItem(R.id.save).isEnabled = true
                    codeContainer.error = null
                }
                ValidationResult.TOO_SHORT -> {
                    toolbar.menu.findItem(R.id.save).isEnabled = false
                    codeContainer.error = null
                }
                ValidationResult.TOO_LONG -> {
                    toolbar.menu.findItem(R.id.save).isEnabled = false
                    codeContainer.error =
                        getString(R.string.referrals_change_code_sheet_error_max_length)
                }
            }
        }
        code.setText(currentCode)

        model.isSubmitting.observe(this) { iss ->
            if (iss == null) {
                return@observe
            }
            isSubmitting = iss

            toolbar.menu.findItem(R.id.save).let { save ->
                if (isSubmitting) {
                    save.actionView = layoutInflater.inflate(
                        R.layout.toolbar_loading_spinner,
                        null
                    )
                    save.isEnabled = false
                } else {
                    save.actionView = null
                    save.isEnabled = true
                }
            }
        }

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
                urcc.asCodeTooShort?.let {
                    codeContainer.error =
                        getString(R.string.referrals_change_code_sheet_general_error)
                    return@observe
                }
                urcc.asCodeTooLong?.let {
                    codeContainer.error =
                        getString(R.string.referrals_change_code_sheet_error_max_length)
                    return@observe
                }
                urcc.asExceededMaximumUpdates?.let {
                    codeContainer.error =
                        getString(R.string.referrals_change_code_sheet_general_error)
                    return@observe
                }

                codeContainer.error =
                    getString(R.string.referrals_change_code_sheet_general_error)
                return@observe
            }
        }
    }

    private fun applyInsets(toolbarHeight: Int) {
        scrollView.updatePadding(top = toolbarHeight)
    }

    companion object {
        private enum class ValidationResult {
            VALID,
            TOO_SHORT,
            TOO_LONG
        }

        private fun validate(code: String): ValidationResult {
            if (code.isBlank()) {
                return ValidationResult.TOO_SHORT
            }

            if (code.length >= 24) {
                return ValidationResult.TOO_LONG
            }

            return ValidationResult.VALID
        }

        private const val CODE = "CODE"
        fun newInstance(context: Context, currentCode: String) =
            Intent(context, ReferralsEditCodeActivity::class.java).apply {
                putExtra(CODE, currentCode)
            }
    }
}
