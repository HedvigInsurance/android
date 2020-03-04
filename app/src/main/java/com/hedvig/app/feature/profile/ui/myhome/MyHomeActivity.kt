package com.hedvig.app.feature.profile.ui.myhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.type.InsuranceType
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatSetTint
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.setupLargeTitle
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.interpolateTextKey
import kotlinx.android.synthetic.main.activity_my_home.*
import kotlinx.android.synthetic.main.additional_buildings_row.view.*
import kotlinx.android.synthetic.main.loading_spinner.*
import kotlinx.android.synthetic.main.sphere_container.*
import org.koin.android.viewmodel.ext.android.viewModel

class MyHomeActivity : BaseActivity() {
    val profileViewModel: ProfileViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_home)

        setupLargeTitle(R.string.PROFILE_MY_HOME_TITLE, R.font.circular_bold, R.drawable.ic_back) {
            onBackPressed()
        }
        sphere.drawable.compatSetTint(compatColor(R.color.maroon))

        changeHomeInformation.setOnClickListener {
            ChangeHomeInfoDialog().show(supportFragmentManager, ChangeHomeInfoDialog.TAG)
        }

        profileViewModel.data.observe(lifecycleOwner = this) { profileData ->
            profileData?.insurance?.let { bind(it) }
        }
    }

    private fun bind(data: ProfileQuery.Insurance) {
        loadingSpinner.remove()
        sphereContainer.show()
        sphereText.text = data.address
        address.text = data.address
        postalNumber.text = data.postalNumber
        insuranceType.text =
            when (data.type) {
                InsuranceType.BRF -> resources.getString(R.string.PROFILE_MY_HOME_INSURANCE_TYPE_BRF)
                InsuranceType.STUDENT_BRF -> resources.getString(R.string.PROFILE_MY_HOME_INSURANCE_TYPE_BRF)
                InsuranceType.RENT -> resources.getString(R.string.PROFILE_MY_HOME_INSURANCE_TYPE_RENT)
                InsuranceType.STUDENT_RENT -> resources.getString(R.string.PROFILE_MY_HOME_INSURANCE_TYPE_RENT)
                InsuranceType.HOUSE -> getString(R.string.MY_HOME_INSURANCE_TYPE_HOUSE)
                else -> ""
            }
        livingSpace.text = interpolateTextKey(
            resources.getString(R.string.PROFILE_MY_HOME_SQUARE_METER_POSTFIX),
            "SQUARE_METER" to data.livingSpace.toString()
        )
        bindHouseData(data)
        infoContainer.show()

        data.extraBuildings?.let { extraBuildings ->
            bindExtraBuildings(extraBuildings)
        } ?: run {
            removeExtraBuildingsViews()
        }
    }

    private fun bindHouseData(data: ProfileQuery.Insurance) {
        bindOrHide(
            data.ancillaryArea?.toString(),
            ancillaryAreaLabel,
            ancillaryArea
        )
        bindOrHide(
            data.yearOfConstruction?.toString(),
            yearOfConstructionLabel,
            yearOfConstruction
        )
        bindOrHide(
            data.numberOfBathrooms?.toString(),
            bathroomsLabel,
            bathrooms
        )
        bindOrHide(data.isSubleted?.let {
            if (it) resources.getString(R.string.HOUSE_INFO_SUBLETED_TRUE) else resources.getString(
                R.string.HOUSE_INFO_SUBLETED_FALSE
            )
        }, subletedLabel, subleted)
    }

    private fun bindExtraBuildings(extraBuildings: List<ProfileQuery.ExtraBuilding>) {
        if (extraBuildings.isEmpty()) {
            removeExtraBuildingsViews()
            return
        }
        additionalBuildingsTitle.show()

        extraBuildings.forEach { eb ->
            val extraBuilding = eb.asExtraBuildingCore ?: return@forEach
            val row = LayoutInflater
                .from(additionalBuildingsContainer.context)
                .inflate(
                    R.layout.additional_buildings_row,
                    additionalBuildingsContainer,
                    false
                )
            row.title.text = extraBuilding.displayName

            var bodyText = interpolateTextKey(
                resources.getString(R.string.HOUSE_INFO_BOYTA_SQUAREMETERS),
                "HOUSE_INFO_AMOUNT_BOYTA" to extraBuilding.area
            )
            if (extraBuilding.hasWaterConnected) {
                bodyText += ", " + resources.getString(R.string.HOUSE_INFO_CONNECTED_WATER)
            }
            row.body.text = bodyText
            additionalBuildingsContainer.addView(row)
        }

        additionalBuildingsContainer.show()
    }

    private fun removeExtraBuildingsViews() {
        additionalBuildingsTitle.remove()
        additionalBuildingsContainer.remove()
    }

    private fun bindOrHide(text: String?, label: View, body: TextView) {
        text?.let {
            label.show()
            body.text = it
            body.show()
        } ?: run {
            label.remove()
            body.remove()
        }
    }
}
