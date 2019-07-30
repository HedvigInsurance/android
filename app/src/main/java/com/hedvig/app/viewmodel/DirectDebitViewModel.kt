package com.hedvig.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.graphql.DirectDebitQuery
import com.hedvig.app.data.debit.DirectDebitRepository
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class DirectDebitViewModel(
    private val directDebitRepository: DirectDebitRepository
) : ViewModel() {
    val data: MutableLiveData<DirectDebitQuery.Data> = MutableLiveData()

    private val disposables = CompositeDisposable()

    init {
        fetchDirectDebit()
    }

    private fun fetchDirectDebit() {
        val disposable = directDebitRepository.fetchDirectDebit()
            .subscribe({ response ->
                if (response.hasErrors()) {
                    Timber.e("Got errors when fetching direct debit: %s", response.errors().toString())
                } else {
                    data.postValue(response.data())
                }
            }, { error ->
                Timber.e(error, "Failed to load direct debit data")
            })
        disposables.add(disposable)
    }

    fun refreshDirectDebitStatus() {
        val disposable = directDebitRepository.refreshDirectdebitStatus()
            .subscribe({ response ->
                response.data()?.let { data ->
                    directDebitRepository.writeDirectDebitStatusToCache(data.directDebitStatus)
                } ?: Timber.e("Failed to refresh direct debit status")
            }, { error ->
                Timber.e(error, "Failed to refresh direct debit status")
            })

        disposables.add(disposable)
    }
}
