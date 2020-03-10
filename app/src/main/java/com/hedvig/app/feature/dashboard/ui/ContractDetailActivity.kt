package com.hedvig.app.feature.dashboard.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.compatSetTint
import com.hedvig.app.util.extensions.isDarkThemeActive
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import e
import kotlinx.android.synthetic.main.activity_contract_detail.*
import org.koin.android.viewmodel.ext.android.viewModel

class ContractDetailActivity : BaseActivity(R.layout.activity_contract_detail) {
    private val model: ContractDetailViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbar.navigationIcon = compatDrawable(R.drawable.ic_close)?.apply {
            if (isDarkThemeActive) {
                compatSetTint(compatColor(R.color.icon_tint))
            }
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

    private fun bind(data: Contract) {
        data.currentAgreement.asNorwegianHomeContentAgreement?.let { nhca ->
            bindHomeInformation(nhca.address.fragments.addressFragment, nhca.squareMeters, "TODO")
            bindCoinsured(nhca.numberCoInsured)
        }
        data.currentAgreement.asSwedishApartmentAgreement?.let { saa ->
            bindHomeInformation(saa.address.fragments.addressFragment, saa.squareMeters, "TODO")
            bindCoinsured(saa.numberCoInsured)
        }
        data.currentAgreement.asSwedishHouseAgreement?.let { sha ->
            bindHomeInformation(sha.address.fragments.addressFragment, sha.squareMeters, "TODO")
            bindCoinsured(sha.numberCoInsured)
        }
        data.currentAgreement.asNorwegianTravelAgreement?.let { nta ->
            bindCoinsured(nta.numberCoInsured)
        }
    }

    private fun bindHomeInformation(addressData: AddressFragment, sqm: Int, typeTranslated: String) {
        homeInformationContainer.show()
        address.text = addressData.street
        squareMeters.text = "$sqm kvm" // TODO
        type.text = typeTranslated
    }

    private fun bindCoinsured(amount: Int) {
        coinsuredContainer.show()
        coinsuredAmount.text = if (amount == 1) { // TODO
            "Endast du"
        } else {
            "Du og ${amount - 1} andre"
        }
    }

    companion object {
        private const val ID = "ID"

        fun newInstance(context: Context, id: String) = Intent(context, ContractDetailActivity::class.java).apply {
            putExtra(ID, id)
        }
    }
}
