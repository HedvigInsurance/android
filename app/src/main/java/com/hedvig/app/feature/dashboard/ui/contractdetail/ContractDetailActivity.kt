package com.hedvig.app.feature.dashboard.ui.contractdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.type.NorwegianHomeContentLineOfBusiness
import com.hedvig.android.owldroid.type.SwedishApartmentLineOfBusiness
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.interpolateTextKey
import e
import kotlinx.android.synthetic.main.activity_contract_detail.*
import org.koin.android.viewmodel.ext.android.viewModel

class ContractDetailActivity : BaseActivity(R.layout.activity_contract_detail) {
    private val model: ContractDetailViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val id = intent.getStringExtra(ID)
        if (id == null) {
            e { "Programmer error: ID not provided to ${this.javaClass}" }
            return
        }

        homeChangeInfo.setHapticClickListener {
            showChangeInfoDialog()
        }

        coinsuredChangeInfo.setHapticClickListener {
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
                startClosableChat()
            }
        )
    }

    private fun bind(data: DashboardQuery.Contract) {
        data.currentAgreement.asNorwegianHomeContentAgreement?.let { nhca ->
            bindHomeInformation(nhca.address.fragments.addressFragment, nhca.squareMeters, nhca.nhcType?.displayName(this)
                ?: "")
            bindCoinsured(nhca.numberCoInsured)
        }
        data.currentAgreement.asSwedishApartmentAgreement?.let { saa ->
            bindHomeInformation(saa.address.fragments.addressFragment, saa.squareMeters, saa.saType.displayName(this))
            bindCoinsured(saa.numberCoInsured)
        }
        data.currentAgreement.asSwedishHouseAgreement?.let { sha ->
            bindHomeInformation(sha.address.fragments.addressFragment, sha.squareMeters, getString(R.string.SWEDISH_HOUSE_LOB))
            bindCoinsured(sha.numberCoInsured)
        }
        data.currentAgreement.asNorwegianTravelAgreement?.let { nta ->
            bindCoinsured(nta.numberCoInsured)
        }
    }

    private fun bindHomeInformation(addressData: AddressFragment, sqm: Int, typeTranslated: String) {
        homeInformationContainer.show()
        address.text = addressData.street
        squareMeters.text = interpolateTextKey(
            getString(R.string.CONTRACT_DETAIL_HOME_SIZE_INPUT),
            "SQUARE_METERS" to sqm
        )
        type.text = typeTranslated
    }

    private fun bindCoinsured(amount: Int) {
        coinsuredContainer.show()
        coinsuredAmount.text = interpolateTextKey(
            getString(R.string.CONTRACT_DETAIL_COINSURED_NUMBER_INPUT),
            "COINSURED" to amount
        )
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

        fun newInstance(context: Context, id: String) = Intent(context, ContractDetailActivity::class.java).apply {
            putExtra(ID, id)
        }
    }
}
