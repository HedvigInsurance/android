package com.hedvig.app.feature.birthday.data

import io.reactivex.Observable

class BirthdayRepository {


    fun getBirthDate(): Observable<String> {
        return Observable.just("199701171234")
    }
}
