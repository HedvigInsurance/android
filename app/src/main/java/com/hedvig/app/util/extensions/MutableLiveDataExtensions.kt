package com.hedvig.app.util.extensions

import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.default(initalValue: T): MutableLiveData<T> {
    apply { setValue(initalValue) }
    return this
}
