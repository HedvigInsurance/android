package com.hedvig.app.feature.profile.ui.myinfo

import android.os.Bundle
import android.text.TextWatcher
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.lifecycle.Observer
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatSetTint
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.extensions.setupLargeTitle
import com.hedvig.app.util.extensions.view.dismissKeyboard
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.validateEmail
import com.hedvig.app.util.validatePhoneNumber
import kotlinx.android.synthetic.main.activity_my_info.*
import kotlinx.android.synthetic.main.loading_spinner.*
import kotlinx.android.synthetic.main.sphere_container.*
import org.koin.android.viewmodel.ext.android.viewModel

class MyInfoActivity : BaseActivity(R.layout.activity_my_info) {
    private val profileViewModel: ProfileViewModel by viewModel()

    private var emailTextWatcher: TextWatcher? = null
    private var phoneNumberTextWatcher: TextWatcher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupLargeTitle(R.string.PROFILE_MY_INFO_TITLE, R.drawable.ic_back) {
            onBackPressed()
        }

        sphere.drawable.compatSetTint(compatColor(R.color.dark_purple))

        loadData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.my_info_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val dirty = profileViewModel.dirty.value
        if (dirty == null || !dirty) {
            menu.removeItem(R.id.save)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val prevEmail = profileViewModel.data.value?.member?.email ?: ""
        val prevPhoneNumber = profileViewModel.data.value?.member?.phoneNumber ?: ""

        val newEmail = emailInput.text.toString()
        val newPhoneNumber = phoneNumberInput.text.toString()

        if (prevEmail != newEmail && !validateEmail(newEmail).isSuccessful) {
            provideValidationNegativeHapticFeedback()
            ValidationDialog.newInstance(
                R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_TITLE,
                R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_DESCRIPTION_EMAIL,
                R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_DISMISS
            ).show(supportFragmentManager, ValidationDialog.TAG)
            return true
        }

        if (prevPhoneNumber != newPhoneNumber && !validatePhoneNumber(newPhoneNumber).isSuccessful) {
            provideValidationNegativeHapticFeedback()
            ValidationDialog.newInstance(
                R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_TITLE,
                R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_DESCRIPTION_PHONE_NUMBER,
                R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_DISMISS
            ).show(supportFragmentManager, ValidationDialog.TAG)
            return true
        }

        profileViewModel.saveInputs(emailInput.text.toString(), phoneNumberInput.text.toString())
        if (emailInput.isFocused) {
            emailInput.clearFocus()
        }
        if (phoneNumberInput.isFocused) {
            phoneNumberInput.clearFocus()
        }
        myInfoRoot.dismissKeyboard()
        return true
    }

    private fun provideValidationNegativeHapticFeedback() =
        findViewById<ActionMenuItemView>(R.id.save)?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

    private fun loadData() {
        profileViewModel.data.observe(this, Observer { profileData ->
            loadingSpinner.remove()
            sphereContainer.show()

            contactDetailsContainer.show()

            profileData?.let { data ->
                sphereText.text = resources.getString(
                    R.string.PROFILE_MY_INFO_NAME_SPHERE,
                    data.member.firstName,
                    data.member.lastName
                )
                setupEmailInput(data.member.email ?: "")
                setupPhoneNumberInput(data.member.phoneNumber ?: "")
            }

            profileViewModel.dirty.observe(this, Observer {
                invalidateOptionsMenu()
            })
        })
    }

    private fun setupEmailInput(prefilledEmail: String) {
        emailTextWatcher?.let { emailInput.removeTextChangedListener(it) }
        emailInput.setText(prefilledEmail)

        emailTextWatcher = emailInput.onChange { value ->
            profileViewModel.emailChanged(value)
            if (emailInputContainer.isErrorEnabled) {
                val validationResult = validateEmail(value)
                if (validationResult.isSuccessful) {
                    emailInputContainer.isErrorEnabled = false
                }
            }
        }

        emailInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                return@setOnFocusChangeListener
            }

            val validationResult = validateEmail(emailInput.text.toString())
            if (!validationResult.isSuccessful) {
                emailInputContainer.error = getString(validationResult.errorTextKey!!)
            }
        }
    }

    private fun setupPhoneNumberInput(prefilledPhoneNumber: String) {
        phoneNumberTextWatcher?.let { phoneNumberInput.removeTextChangedListener(it) }
        phoneNumberInput.setText(prefilledPhoneNumber)

        phoneNumberTextWatcher = phoneNumberInput.onChange { value ->
            profileViewModel.phoneNumberChanged(value)
            if (phoneNumberInputContainer.isErrorEnabled) {
                val validationResult = validatePhoneNumber(value)
                if (validationResult.isSuccessful) {
                    phoneNumberInputContainer.isErrorEnabled = false
                }
            }
        }

        phoneNumberInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                return@setOnFocusChangeListener
            }
            val validationResult = validatePhoneNumber(phoneNumberInput.text.toString())
            if (!validationResult.isSuccessful) {
                phoneNumberInputContainer.error = getString(validationResult.errorTextKey!!)
            }
        }
    }
}
