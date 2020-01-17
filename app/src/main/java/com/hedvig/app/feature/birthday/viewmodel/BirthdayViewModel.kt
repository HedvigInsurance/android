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

    private val birthDate = birthdayRepository.getBirthDate()

    val isBirthdayData: MutableLiveData<Boolean> = MutableLiveData()
    private val disposables = CompositeDisposable()



    fun isBirthday() {

        val birthMonth = birthDate.substring(4, birthDate.length - 6).toInt()
        val birthDay = birthDate.substring(6, birthDate.length - 4).toInt()


        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        Timber.d("current date: $currentMonth/$currentDay Birth date $birthMonth/$birthDay")

        if (currentDay == birthDay && currentMonth == birthMonth) {
            isBirthdayData.postValue(true)
            Timber.d("bday True")
        } else {
            Timber.d("bday False")
            isBirthdayData.postValue(false)
        }
    }

    fun getBirthDay() {
        disposables += birthdayRepository.getBirthDate()
            .subscribe()
    }
}
