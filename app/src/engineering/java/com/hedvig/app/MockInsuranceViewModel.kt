package com.hedvig.app

import android.content.Context
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.DevelopmentScreenAdapter.ViewHolder.Header.Companion.DEVELOPMENT_PREFERENCES
import com.hedvig.app.feature.insurance.ui.InsuranceViewModel
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_NORWEGIAN_TRAVEL
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_APARTMENT
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_HOUSE

class MockInsuranceViewModel(context: Context) : InsuranceViewModel() {
    override fun load() {}

    init {
        val activePersona = context
            .getSharedPreferences(DEVELOPMENT_PREFERENCES, Context.MODE_PRIVATE)
            .getInt("mockPersona", 0)
        data.postValue(
            when (activePersona) {
                0 -> Result.success(SWEDISH_APARTMENT)
                1 -> Result.success(SWEDISH_HOUSE)
                2 -> Result.success(NORWEGIAN_HOME_CONTENTS)
                3 -> Result.success(NORWEGIAN_TRAVEL)
                4 -> Result.success(NORWEGIAN_HOME_CONTENTS_AND_TRAVEL)
                else -> Result.success(SWEDISH_APARTMENT)
            }
        )
    }

    companion object {

        val SWEDISH_APARTMENT = INSURANCE_DATA_SWEDISH_APARTMENT

        val SWEDISH_HOUSE = INSURANCE_DATA_SWEDISH_HOUSE

        val NORWEGIAN_HOME_CONTENTS = INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS

        val NORWEGIAN_TRAVEL = INSURANCE_DATA_NORWEGIAN_TRAVEL

        val NORWEGIAN_HOME_CONTENTS_AND_TRAVEL = InsuranceQuery.Data(
            listOf(
                INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS.contracts[0],
                INSURANCE_DATA_NORWEGIAN_TRAVEL.contracts[0]
            )
        )
    }
}
