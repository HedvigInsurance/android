package com.hedvig.app.feature.welcome

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerPage
import com.hedvig.app.util.apollo.ThemedIconUrls
import e
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

class WelcomeViewModel(
    private val welcomeRepository: WelcomeRepository
) : ViewModel() {

    val data = MutableLiveData<List<DismissiblePagerPage>>()

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
                        response.welcome.map { page ->
                            DismissiblePagerPage(
                                ThemedIconUrls.from(page.illustration.variants.fragments.iconVariantsFragment),
                                page.title,
                                page.paragraph
                            )
                        })
                } ?: e { "No welcome data" }
            }, { e(it) })
    }
}
