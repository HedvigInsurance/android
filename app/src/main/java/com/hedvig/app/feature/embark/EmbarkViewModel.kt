package com.hedvig.app.feature.embark

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import kotlinx.coroutines.launch

abstract class EmbarkViewModel : ViewModel() {
    abstract val data: LiveData<EmbarkStoryQuery.Data>

    abstract fun load(name: String)
}

class EmbarkViewModelImpl(
    private val embarkRepository: EmbarkRepository
) : EmbarkViewModel() {
    override val data = MutableLiveData<EmbarkStoryQuery.Data>()

    override fun load(name: String) {
        viewModelScope.launch {
            val result = runCatching {
                embarkRepository
                    .embarkStoryAsync(name)
                    .await()
            }

            result.getOrNull()?.data()?.let { d ->
                data.postValue(d)
            }
        }
    }
}
