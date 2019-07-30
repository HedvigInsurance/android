package com.hedvig.app.feature.welcome

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.graphql.WelcomeQuery
import com.hedvig.app.feature.dismissablepager.DismissablePagerPage
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber

class WelcomeViewModel(
    private val welcomeRepository: WelcomeRepository
) : ViewModel() {

    val data = MutableLiveData<List<DismissablePagerPage>>()

    private val disposables = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    fun fetch() {
        disposables += welcomeRepository
            .fetchWelcomeScreens()
            .subscribe({
                it?.let { response ->
                    data.postValue(
                        response.welcome.map {
                            DismissablePagerPage(it.illustration.svgUrl, it.title, it.paragraph)
                        })
                } ?: Timber.e("No welcome data")
            }, { Timber.e(it) })
    }
}
