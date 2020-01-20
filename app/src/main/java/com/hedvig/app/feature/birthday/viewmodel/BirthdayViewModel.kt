package com.hedvig.app.feature.birthday.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.app.feature.birthday.data.BirthdayRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber
import java.util.*


class BirthdayViewModel(
    private val birthdayRepository: BirthdayRepository
) : ViewModel() {

    private lateinit var birthDate: String

    val isBirthdayData: MutableLiveData<Boolean> = MutableLiveData()
    private val disposables = CompositeDisposable()



    private fun isBirthday(): Boolean {

        val birthMonth = birthDate.substring(4, birthDate.length - 6).toInt()
        val birthDay = birthDate.substring(6, birthDate.length - 4).toInt()

        val calendar = Calendar.getInstance()

        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        return currentDay == birthDay && currentMonth == birthMonth
    }

    private fun getBirthDay() {
        disposables += birthdayRepository.fetchBirthDate()
            .subscribe({
                birthDate = it
                isBirthdayData.postValue(isBirthday())
            }, {
                isBirthdayData.postValue(false)
            })
    }

    init {
        getBirthDay()
    }

    override fun onCleared() {
        super.onCleared()

        disposables.clear()
    }
}
