package com.hedvig.app.feature.birthday.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.app.feature.birthday.data.ProfileRepository
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.rxkotlin.plusAssign
import java.util.*


class BirthdayViewModel(
    private val prfileRepository: ProfileRepository
) : ViewModel() {

    private val birthDate = prfileRepository.getBirthDate()

//    private val isBirthday = MutableLiveData<Boolean>()
//
//    private lateinit var observable: Observable<Boolean>
//    lateinit var observer: Observer

    private val isBirthdayData: MutableLiveData<Boolean> = MutableLiveData()
    private val disposables = CompositeDisposable()

    fun isBirthday() {

        val birthMonth = birthDate.substring(4, birthDate.length - 6).toInt()
        val birthDay = birthDate.substring(6, birthDate.length - 4).toInt()


        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

        if (currentDay == birthDay && currentMonth == birthMonth) {
            isBirthdayData.postValue(true)
        } else {
            isBirthdayData.postValue(false)
        }
    }
}
