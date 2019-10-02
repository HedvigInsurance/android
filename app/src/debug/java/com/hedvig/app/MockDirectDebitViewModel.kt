package com.hedvig.app

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.graphql.DirectDebitQuery
import com.hedvig.app.viewmodel.DirectDebitViewModel
import type.DirectDebitStatus

class MockDirectDebitViewModel : DirectDebitViewModel() {
    override val data = MutableLiveData<DirectDebitQuery.Data>()

    init {
        data.postValue(
            DirectDebitQuery.Data(
                DirectDebitStatus.NEEDS_SETUP
            )
        )
    }

    override fun refreshDirectDebitStatus() = Unit
}
