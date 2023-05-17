package com.hedvig.app.feature.referrals.ui.activated

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.app.feature.loggedin.ui.LoggedInRepository
import giraffe.LoggedInQuery
import kotlinx.coroutines.launch
import slimber.log.e

abstract class ReferralsActivatedViewModel : ViewModel() {
  abstract val data: LiveData<LoggedInQuery.Data>
}

class ReferralsActivatedViewModelImpl(
  private val loggedInRepository: LoggedInRepository,
) : ReferralsActivatedViewModel() {
  override val data = MutableLiveData<LoggedInQuery.Data>()

  init {
    viewModelScope.launch {
      when (val loggedInData = loggedInRepository.loggedInData()) {
        is Either.Left -> e { "loggedInData failed to fetch: ${loggedInData.value.message}" }
        is Either.Right -> loggedInData.value.let { data.postValue(it) }
      }
    }
  }
}
