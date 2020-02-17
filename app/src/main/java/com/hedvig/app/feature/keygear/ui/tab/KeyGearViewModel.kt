package com.hedvig.app.feature.keygear.ui.tab

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.app.feature.keygear.data.KeyGearItemsRepository
import kotlinx.coroutines.launch

abstract class KeyGearViewModel : ViewModel() {
    abstract val data: LiveData<KeyGearItemsQuery.Data>
}

class KeyGearViewModelImpl(
    private val repository: KeyGearItemsRepository
) : KeyGearViewModel() {
    override val data = MutableLiveData<KeyGearItemsQuery.Data>()

    init {
        viewModelScope.launch {
            for (response in repository.keyGearItems()) {
                data.postValue(response.data())
            }
        }
    }
}
