package com.hedvig.app.common

fun interface Mapper<From, To> {
  suspend fun map(from: From): To
}
