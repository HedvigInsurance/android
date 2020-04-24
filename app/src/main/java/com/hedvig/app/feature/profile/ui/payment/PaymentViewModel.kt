package com.hedvig.app.feature.profile.ui.payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.data.debit.PayinStatusRepository
import com.hedvig.app.feature.profile.data.ProfileRepository
import com.zhuinden.livedatacombinetuplekt.combineTuple
import e
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class PaymentViewModel : ViewModel() {
    abstract val data: LiveData<Pair<ProfileQuery.Data?, PayinStatusQuery.Data?>>
}

class PaymentViewModelImpl(
    private val profileRepository: ProfileRepository,
    private val payinStatusRepository: PayinStatusRepository
) : PaymentViewModel() {
    private val profileData = MutableLiveData<ProfileQuery.Data>()
    private val payinStatusData = MutableLiveData<PayinStatusQuery.Data>()

    override val data = combineTuple(profileData, payinStatusData)

    init {
        viewModelScope.launch {
            profileRepository
                .profile()
                .onEach { profileData.postValue(it.data()) }
                .catch { e(it) }
                .launchIn(this)

            payinStatusRepository
                .payinStatus()
                .onEach { payinStatusData.postValue(it.data()) }
                .catch { e(it) }
                .launchIn(this)
        }
    }
}
