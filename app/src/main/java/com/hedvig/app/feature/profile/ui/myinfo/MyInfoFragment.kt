package com.hedvig.app.feature.profile.ui.myinfo

import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.view.menu.ActionMenuItemView
import android.text.TextWatcher
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatSetTint
import com.hedvig.app.util.extensions.hideKeyboard
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.extensions.setupLargeTitle
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.interpolateTextKey
import com.hedvig.app.util.validateEmail
import com.hedvig.app.util.validatePhoneNumber
import kotlinx.android.synthetic.main.fragment_my_info.*
import kotlinx.android.synthetic.main.loading_spinner.*
import kotlinx.android.synthetic.main.sphere_container.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class MyInfoFragment : androidx.fragment.app.Fragment() {
    private val profileViewModel: ProfileViewModel by sharedViewModel()

    private var emailTextWatcher: TextWatcher? = null
    private var phoneNumberTextWatcher: TextWatcher? = null

    private val navController: NavController by lazy {
        requireActivity().findNavController(R.id.loggedNavigationHost)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_my_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLargeTitle(R.string.PROFILE_MY_INFO_TITLE, R.font.circular_bold, R.drawable.ic_back) {
            navController.popBackStack()
        }

        sphere.drawable.compatSetTint(requireContext().compatColor(R.color.dark_purple))

        loadData()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.my_info_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val dirty = profileViewModel.dirty.value
        if (dirty == null || !dirty) {
            menu.removeItem(R.id.save)
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val prevEmail = profileViewModel.data.value?.member?.email ?: ""
        val prevPhoneNumber = profileViewModel.data.value?.member?.phoneNumber ?: ""

        val newEmail = emailInput.text.toString()
        val newPhoneNumber = phoneNumberInput.text.toString()

        if (prevEmail != newEmail && !validateEmail(newEmail).isSuccessful) {
            provideValidationNegativeHapticFeedback()
            fragmentManager?.let { fm ->
                val dialog =
                    ValidationDialog.newInstance(
                        R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_TITLE,
                        R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_DESCRIPTION_EMAIL,
                        R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_DISMISS
                    )
                val transaction = fm.beginTransaction()
                val prev = fm.findFragmentByTag("validation")
                prev?.let { transaction.remove(it) }
                transaction.addToBackStack(null)
                dialog.show(transaction, "validation")
            }
            return true
        }

        if (prevPhoneNumber != newPhoneNumber && !validatePhoneNumber(newPhoneNumber).isSuccessful) {
            provideValidationNegativeHapticFeedback()
            fragmentManager?.let { fm ->
                val dialog =
                    ValidationDialog.newInstance(
                        R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_TITLE,
                        R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_DESCRIPTION_PHONE_NUMBER,
                        R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_DISMISS
                    )
                val transaction = fm.beginTransaction()
                val prev = fm.findFragmentByTag("validation")
                prev?.let { transaction.remove(it) }
                transaction.addToBackStack(null)
                dialog.show(transaction, "validation")
            }
            return true
        }

        profileViewModel.saveInputs(emailInput.text.toString(), phoneNumberInput.text.toString())
        if (emailInput.isFocused) {
            emailInput.clearFocus()
        }
        if (phoneNumberInput.isFocused) {
            phoneNumberInput.clearFocus()
        }
        view?.let { requireContext().hideKeyboard(it) }
        return true
    }

    private fun provideValidationNegativeHapticFeedback() =
        view?.findViewById<ActionMenuItemView>(R.id.save)?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

    private fun loadData() {
        profileViewModel.data.observe(this, Observer { profileData ->
            setHasOptionsMenu(true)
            loadingSpinner.remove()
            sphereContainer.show()

            contactDetailsContainer.show()

            profileData?.let { data ->
                sphereText.text = interpolateTextKey(
                    resources.getString(R.string.PROFILE_MY_INFO_NAME_SPHERE),
                    "FIRST_NAME" to data.member.firstName,
                    "LAST_NAME" to data.member.lastName
                )
                setupEmailInput(data.member.email ?: "")
                setupPhoneNumberInput(data.member.phoneNumber ?: "")
            }

            profileViewModel.dirty.observe(this, Observer {
                activity?.invalidateOptionsMenu()
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
                emailInputContainer.error = requireContext().getString(validationResult.errorTextKey!!)
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

                phoneNumberInputContainer.error = requireContext().getString(validationResult.errorTextKey!!)
            }
        }
    }
}
