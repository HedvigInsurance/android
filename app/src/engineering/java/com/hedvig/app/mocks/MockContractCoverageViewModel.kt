package com.hedvig.app.mocks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.DevelopmentScreenAdapter.ViewHolder.Header.Companion.DEVELOPMENT_PREFERENCES
import com.hedvig.app.MockInsuranceViewModel
import com.hedvig.app.feature.insurance.ui.contractcoverage.ContractCoverageViewModel

class MockContractCoverageViewModel(context: Context) : ContractCoverageViewModel() {
    override val data = MutableLiveData<InsuranceQuery.Contract>()

    private val mockData: InsuranceQuery.Data

    init {
        val mockPersona = context
            .getSharedPreferences(DEVELOPMENT_PREFERENCES, Context.MODE_PRIVATE)
            .getInt("mockPersona", 0)

        mockData = when (mockPersona) {
            0 -> MockInsuranceViewModel.SWEDISH_APARTMENT
            1 -> MockInsuranceViewModel.SWEDISH_HOUSE
            2 -> MockInsuranceViewModel.NORWEGIAN_HOME_CONTENTS
            3 -> MockInsuranceViewModel.NORWEGIAN_TRAVEL
            4 -> MockInsuranceViewModel.NORWEGIAN_HOME_CONTENTS_AND_TRAVEL
            else -> MockInsuranceViewModel.SWEDISH_APARTMENT
        }
    }

    override fun loadContract(id: String) {
        data.value = mockData.contracts.find { it.id == id }
    }
}
