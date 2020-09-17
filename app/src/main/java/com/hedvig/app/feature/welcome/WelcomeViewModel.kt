package com.hedvig.app.feature.welcome

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.R
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerModel
import com.hedvig.app.util.apollo.ThemedIconUrls
import e
import kotlinx.coroutines.launch

class WelcomeViewModel(
    private val welcomeRepository: WelcomeRepository,
    application: Application
) : AndroidViewModel(application) {

    val data = MutableLiveData<List<DismissiblePagerModel>>()

    fun fetch() {
        viewModelScope.launch {
            val response = runCatching { welcomeRepository.fetchWelcomeScreensAsync().await() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data?.let { response ->
                data.postValue(
                    response.welcome.mapIndexed { index, page ->
                        if (index == response.welcome.size - 1) {
                            DismissiblePagerModel.TitlePage(
                                ThemedIconUrls.from(page.illustration.variants.fragments.iconVariantsFragment),
                                page.title,
                                page.paragraph,
                                getApplication<Application>().getString(R.string.NEWS_DISMISS)
                            )
                        } else {
                            DismissiblePagerModel.TitlePage(
                                ThemedIconUrls.from(page.illustration.variants.fragments.iconVariantsFragment),
                                page.title,
                                page.paragraph,
                                getApplication<Application>().getString(R.string.NEWS_PROCEED)
                            )
                        }
                    })
            }
        }
    }
}
