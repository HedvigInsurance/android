package com.hedvig.app.util.extensions

import arrow.core.Either
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

inline fun <L, R, N> Flow<Either<L, R>>.mapEitherRight(
    crossinline transform: suspend (value: R) -> N
): Flow<Either<L, N>> {
    return transform { value ->
        return@transform emit(value.map { transform(it) })
    }
}
