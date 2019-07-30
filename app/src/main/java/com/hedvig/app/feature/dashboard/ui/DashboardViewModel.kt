package com.hedvig.app.feature.dashboard.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.app.feature.chat.ChatRepository
import com.hedvig.app.feature.dashboard.data.DashboardRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber

class DashboardViewModel(
    val dashboardRepository: DashboardRepository,
    val chatRepository: ChatRepository
) : ViewModel() {

    val data = MutableLiveData<DashboardQuery.Data>()

    val disposables = CompositeDisposable()

    init {
        loadData()
    }

    private fun loadData() {
        disposables += dashboardRepository
            .fetchDashboard()
            .subscribe({ response ->
                response.data()?.let { data.postValue(it) }
            }, { Timber.e(it) })
    }
}
