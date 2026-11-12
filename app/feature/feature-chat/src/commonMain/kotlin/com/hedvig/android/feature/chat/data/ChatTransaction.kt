package com.hedvig.android.feature.chat.data

import androidx.room.RoomDatabase

/** Runs [block], and the DAO calls in it, in one write transaction. */
internal expect suspend fun <R> RoomDatabase.chatTransaction(block: suspend () -> R): R
