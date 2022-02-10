package com.hedvig.app.util.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

fun <T> Flow<T>.withHistoryOfLastValue(): Flow<ItemWithHistory<T>> = flow {
    var old: T? = null
    collect { new ->
        emit(ItemWithHistory(old, new))
        old = new
    }
}

data class ItemWithHistory<T>(
    val old: T?,
    val current: T,
)
