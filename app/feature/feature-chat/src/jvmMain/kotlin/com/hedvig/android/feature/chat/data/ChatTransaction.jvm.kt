package com.hedvig.android.feature.chat.data

import androidx.room.RoomDatabase
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection

internal actual suspend fun <R> RoomDatabase.chatTransaction(block: suspend () -> R): R =
  useWriterConnection { transactor -> transactor.immediateTransaction { block() } }
