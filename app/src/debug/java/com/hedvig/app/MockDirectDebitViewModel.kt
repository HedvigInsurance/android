package com.hedvig.app

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.graphql.DirectDebitQuery
import com.hedvig.android.owldroid.type.DirectDebitStatus
import com.hedvig.app.viewmodel.DirectDebitViewModel

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
