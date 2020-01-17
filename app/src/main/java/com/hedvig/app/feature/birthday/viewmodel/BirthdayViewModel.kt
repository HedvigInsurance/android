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


        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        Timber.d("current date: $currentMonth/$currentDay Birth date $birthMonth/$birthDay")

        return if (currentDay == birthDay && currentMonth == birthMonth) {
            Timber.d("bday True")
            true
        } else {
            Timber.d("bday False")
            false
        }
    }

    fun getBirthDay() {
        disposables += birthdayRepository.fetchBirthDate()
            .subscribe({
                birthDate = it
                isBirthdayData.postValue(isBirthday())
            }, {
                error ->
                isBirthdayData.postValue(false)
                Timber.d("Error ${error.localizedMessage}")
            })
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
