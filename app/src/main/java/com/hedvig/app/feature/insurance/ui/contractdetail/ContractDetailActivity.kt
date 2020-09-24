package com.hedvig.app.feature.insurance.ui.contractdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.NorwegianHomeContentLineOfBusiness
import com.hedvig.android.owldroid.type.SwedishApartmentLineOfBusiness
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.insurance.service.InsuranceTracker
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updatePadding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import e
import kotlinx.android.synthetic.main.activity_contract_detail.*
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class ContractDetailActivity : BaseActivity(R.layout.activity_contract_detail) {
    private val model: ContractDetailViewModel by viewModel()
    private val tracker: InsuranceTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root.setEdgeToEdgeSystemUiFlags(true)
        scrollView.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(
                bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom
            )
        }

        toolbar.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
        }


        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val id = intent.getStringExtra(ID)
        if (id == null) {
            e { "Programmer error: ID not provided to ${this.javaClass}" }
            return
        }

        homeChangeInfo.setHapticClickListener {
            tracker.changeHomeInfo()
            showChangeInfoDialog()
        }

        coinsuredChangeInfo.setHapticClickListener {
            tracker.changeCoinsuredInfo()
            showChangeInfoDialog()
        }

        model.data.observe(this) { contract ->
            contract?.let { bind(it) }
        }
        model.loadContract(id)
    }

    private fun showChangeInfoDialog() {
        showAlert(
            R.string.PROFILE_MY_HOME_CHANGE_DIALOG_TITLE,
            R.string.PROFILE_MY_HOME_CHANGE_DIALOG_DESCRIPTION,
            R.string.PROFILE_MY_HOME_CHANGE_DIALOG_CONFIRM,
            R.string.PROFILE_MY_HOME_CHANGE_DIALOG_CANCEL,
            positiveAction = {
                lifecycleScope.launch {
                    model.triggerFreeTextChat()
                    startClosableChat()
                }
            }
        )
    }

    private fun bind(data: InsuranceQuery.Contract) {
        data.currentAgreement.asNorwegianHomeContentAgreement?.let { nhca ->
            bindHomeInformation(
                nhca.address.fragments.addressFragment,
                nhca.squareMeters,
                nhca.nhcType?.displayName(this)
                    ?: ""
            )
            bindCoinsured(nhca.numberCoInsured)
        }
        data.currentAgreement.asSwedishApartmentAgreement?.let { saa ->
            bindHomeInformation(
                saa.address.fragments.addressFragment,
                saa.squareMeters,
                saa.saType.displayName(this)
            )
            bindCoinsured(saa.numberCoInsured)
        }
        data.currentAgreement.asSwedishHouseAgreement?.let { sha ->
            bindHomeInformation(
                sha.address.fragments.addressFragment,
                sha.squareMeters,
                getString(R.string.SWEDISH_HOUSE_LOB)
            )
            bindCoinsured(sha.numberCoInsured)
        }
        data.currentAgreement.asNorwegianTravelAgreement?.let { nta ->
            bindCoinsured(nta.numberCoInsured)
        }
    }

    private fun bindHomeInformation(
        addressData: AddressFragment,
        sqm: Int,
        typeTranslated: String
    ) {
        homeInformationContainer.show()
        address.text = addressData.street
        postalNumber.text = addressData.postalCode
        squareMeters.text = getString(R.string.CONTRACT_DETAIL_HOME_SIZE_INPUT, sqm)
        type.text = typeTranslated
    }

    private fun bindCoinsured(amount: Int) {
        coinsuredContainer.show()
        coinsuredAmount.text = when (amount) {
            0 -> getString(R.string.CONTRACT_DETAIL_COINSURED_NUMBER_INPUT_ZERO_COINSURED)
            1 -> getString(R.string.CONTRACT_DETAIL_COINSURED_NUMBER_INPUT_ONE_COINSURED)
            else -> getString(R.string.CONTRACT_DETAIL_COINSURED_NUMBER_INPUT, amount)
        }
    }

    companion object {
        private const val ID = "ID"

        private fun SwedishApartmentLineOfBusiness.displayName(context: Context) = when (this) {
            SwedishApartmentLineOfBusiness.RENT -> context.getString(R.string.SWEDISH_APARTMENT_LOB_RENT)
            SwedishApartmentLineOfBusiness.BRF -> context.getString(R.string.SWEDISH_APARTMENT_LOB_BRF)
            SwedishApartmentLineOfBusiness.STUDENT_RENT -> context.getString(R.string.SWEDISH_APARTMENT_LOB_STUDENT_RENT)
            SwedishApartmentLineOfBusiness.STUDENT_BRF -> context.getString(R.string.SWEDISH_APARTMENT_LOB_STUDENT_BRF)
            SwedishApartmentLineOfBusiness.UNKNOWN__ -> ""
        }

        private fun NorwegianHomeContentLineOfBusiness.displayName(context: Context) = when (this) {
            NorwegianHomeContentLineOfBusiness.RENT -> context.getString(R.string.NORWEIGIAN_HOME_CONTENT_LOB_RENT)
            NorwegianHomeContentLineOfBusiness.OWN -> context.getString(R.string.NORWEIGIAN_HOME_CONTENT_LOB_OWN)
            NorwegianHomeContentLineOfBusiness.YOUTH_RENT -> context.getString(R.string.NORWEIGIAN_HOME_CONTENT_LOB_STUDENT_RENT)
            NorwegianHomeContentLineOfBusiness.YOUTH_OWN -> context.getString(R.string.NORWEIGIAN_HOME_CONTENT_LOB_STUDENT_OWN)
            NorwegianHomeContentLineOfBusiness.UNKNOWN__ -> ""
        }

        fun newInstance(context: Context, id: String) =
            Intent(context, ContractDetailActivity::class.java).apply {
                putExtra(ID, id)
            }
    }
}
