package com.hedvig.app.feature.travel

import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.type.CreateLuggageClaimInput
import com.hedvig.app.util.LiveEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber

class TravelViewModel(
    private val travelRepository: TravelRepository
) : ViewModel() {

    private val disposables = CompositeDisposable()

    val claimCreationStatus = LiveEvent<Boolean>()

    fun createLuggageClaim(input: CreateLuggageClaimInput) {
        disposables += travelRepository.createLuggageClaim(input)
            .subscribe({ response ->
                if (response.data()?.createLuggageClaim == true) {
                    claimCreationStatus.postValue(true)
                } else {
                    claimCreationStatus.postValue(false)
                }
            }, { Timber.e(it)})
    }
}
