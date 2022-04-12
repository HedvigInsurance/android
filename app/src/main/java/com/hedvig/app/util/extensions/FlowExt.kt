package com.hedvig.app.util.extensions

import arrow.core.Either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlin.time.Duration.Companion.seconds

inline fun <L, R, N> Flow<Either<L, R>>.mapEitherRight(
    crossinline transform: suspend (value: R) -> N
): Flow<Either<L, N>> {
    return transform { value ->
        return@transform emit(value.map { transform(it) })
    }
}

fun <T1, T2> combineState(
    first: Flow<T1>,
    second: Flow<T2>,
    scope: CoroutineScope,
    sharingStarted: SharingStarted = SharingStarted.WhileSubscribed(5.seconds)
): StateFlow<Pair<T1?, T2?>> =
    combine(first, second) { a, b -> Pair(a, b) }.stateIn(scope, sharingStarted, Pair(null, null))
