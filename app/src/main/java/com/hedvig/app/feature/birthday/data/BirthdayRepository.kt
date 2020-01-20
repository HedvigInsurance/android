package com.hedvig.app.feature.birthday.data

import io.reactivex.Observable

class BirthdayRepository {


    fun fetchBirthDate(): Observable<String> {
        return Observable.just("199701201234")
    }
}
