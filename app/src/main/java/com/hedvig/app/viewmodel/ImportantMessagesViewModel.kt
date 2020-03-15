package com.hedvig.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.graphql.ImportantMessagesQuery
import com.hedvig.app.data.ImportantMessagesRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber

class ImportantMessagesViewModel (
    private val importantMessagesRepository: ImportantMessagesRepository
): ViewModel() {

    val data = MutableLiveData<ImportantMessagesQuery.Data>()

    val disposables = CompositeDisposable()

    init {
        loadData()
    }

    private fun loadData() {
        disposables += importantMessagesRepository
            .fetchImportantMessages()
            .subscribe({ response ->
                response.data()?.let { data.postValue(it) }
            }, { Timber.e(it) })
    }
}
