package com.hedvig.android.feature.chat.data

import androidx.room.RoomDatabase
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection

/** Runs [block], and the DAO calls in it, in one write transaction. */
internal suspend fun <R> RoomDatabase.chatTransaction(block: suspend () -> R): R =
  useWriterConnection { transactor -> transactor.immediateTransaction { block() } }
