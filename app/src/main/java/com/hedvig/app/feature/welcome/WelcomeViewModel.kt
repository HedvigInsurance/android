package com.hedvig.app.feature.welcome

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerPage
import com.hedvig.app.util.apollo.ThemedIconUrls
import e
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch

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
        viewModelScope.launch {
            val response = runCatching { welcomeRepository.fetchWelcomeScreens() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data()?.let { response ->
                data.postValue(
                    response.welcome.map { page ->
                        DismissiblePagerPage(
                            ThemedIconUrls.from(page.illustration.variants.fragments.iconVariantsFragment),
                            page.title,
                            page.paragraph
                        )
                    })
            }
        }
    }
}
