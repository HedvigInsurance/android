package com.hedvig.app.feature.profile.ui.myhome

import android.os.Bundle
import androidx.lifecycle.Observer
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatSetTint
import com.hedvig.app.util.extensions.setupLargeTitle
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.interpolateTextKey
import kotlinx.android.synthetic.main.activity_my_home.*
import kotlinx.android.synthetic.main.loading_spinner.*
import kotlinx.android.synthetic.main.sphere_container.*
import org.koin.android.viewmodel.ext.android.viewModel
import type.InsuranceType

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

        loadData()
    }

    private fun loadData() {
        profileViewModel.data.observe(this, Observer { profileData ->
            loadingSpinner.remove()
            sphereContainer.show()

            profileData?.insurance?.let { insuranceData ->
                sphereText.text = insuranceData.address
                postalNumber.text = insuranceData.postalNumber
                insuranceType.text =
                    when (insuranceData.type) {
                        InsuranceType.BRF -> resources.getString(R.string.PROFILE_MY_HOME_INSURANCE_TYPE_BRF)
                        InsuranceType.STUDENT_BRF -> resources.getString(R.string.PROFILE_MY_HOME_INSURANCE_TYPE_BRF)
                        InsuranceType.RENT -> resources.getString(R.string.PROFILE_MY_HOME_INSURANCE_TYPE_RENT)
                        InsuranceType.STUDENT_RENT -> resources.getString(R.string.PROFILE_MY_HOME_INSURANCE_TYPE_RENT)
                        else -> ""
                    }
                livingSpace.text = interpolateTextKey(
                    resources.getString(R.string.PROFILE_MY_HOME_SQUARE_METER_POSTFIX),
                    "SQUARE_METER" to insuranceData.livingSpace.toString()
                )
                infoContainer.show()
            }
        })
    }
}
